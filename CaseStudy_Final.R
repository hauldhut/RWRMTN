################################################################
library(Biobase)
library(GEOquery)
library(limma)
library(httr)
library(jsonlite)

library(httr)
library(jsonlite)

### Load series and platform data from GEO
gset <- getGEO("GSE19783", GSEMatrix =TRUE, AnnotGPL=FALSE)
if (length(gset) > 1){
  idx <- grep("GPL8227", attr(gset, "names"))   
}else{
  idx <- 1
}
  
gset <- gset[[idx]]

# make proper column names to match toptable 
fvarLabels(gset) <- make.names(fvarLabels(gset))

TP53Status<-gset$`tp53 mutation status:ch1`
# labeling for all samples
gsms<-""

sml<-c()
for(i in 1:length(TP53Status)){
  if(TP53Status[i]=="Mut"){
    gsms<-paste0(gsms,"1")
    sml[i]<-"Group1"
  }else{
    gsms<-paste0(gsms,"0")
    sml[i]<-"Group0"
  }
}

#Retrieve Expression Data From ESets
ex <- exprs(gset)
ex[which(ex <= 0)] <- NaN
# log2 transform
exprs(gset) <- log2(ex)

### Differential expression analysis with limma package
# set up the data and proceed with analysis
fl <- as.factor(sml)
gset$description <- fl
#creates a design (or model) matrix
design <- model.matrix(~ description + 0, gset)
colnames(design) <- levels(fl)
fit <- lmFit(gset, design)
cont.matrix <- makeContrasts(Group1-Group0, levels=design)
fit2 <- contrasts.fit(fit, cont.matrix)
fit2 <- eBayes(fit2, 0.01)
diffmiRNAlist <- topTable(fit2, adjust="fdr", number=nrow(fit2))

#Only select miRNAs whose differential expression between the two group (Group1 & Group0) 
#is statistically significant  (adj.P.val <=0.05) for ranking with RWRMTN
sigmiRNAlist <- subset(diffmiRNAlist,adj.P.Val<=0.05) #This returns 85 miRNAs
sigmiRNAlist <- subset(sigmiRNAlist, select=c("ID","adj.P.Val","P.Value"))
colnames(sigmiRNAlist)<-c("rnaName", "adj.P.Val","P.Value")

#Save statistically significant miRNAs standard output
write.table(sigmiRNAlist, file=stdout(), row.names=F, sep="\t")


###Rank statistically significant miRNAs (candidate miRNAs) with RWRMTN
#Get miRNA list
lr<-sigmiRNAlist$rnaName
lor<-""
n<-length(lr)
for(i in 1:n){
  lor<-paste(lor, lr[i],", ",sep='')  
}

#Select datasets (miRTargetDB, miR2DiseaseDB), the disease of interest (MIM114480: Breast cancer) 
#and pass the candidata miRNAs list
login <- list(
  diseaseOMIMID= "MIM114480",#OMIM ID of Breast cancer 
  listOfmiRNAs= lor,#"hsa-miR-125a-5p,hsa-miR-9",
  miRTargetDB= "miRWalk",
  miR2DiseaseDB= "HMDD"
)

#Run Cytosacpe CyREST API
request_body_json <- toJSON(login)
res <- POST("http://localhost:1234/RWRMTN/v1/rank", body = login, encode="json")
y<-httr::content(res,"text", encoding = 'UTF-8')
get_prices_json <- fromJSON(y, flatten = TRUE)

Output <- fromJSON((y))
#Remove miRNA with rank=0 (which are not available on the miRNA-target network)
rankedmiRNAlist <- Output[which(Output$rnaRank!=0),]
rankedmiRNAlist
write.csv(rankedmiRNAlist, file="rankedmiRNAlist.csv", row.names=F)

