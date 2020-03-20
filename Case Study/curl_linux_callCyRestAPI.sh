###Read array from text file:
vars=($(awk -F= '{print $1}' lor.txt))
###Parse to string
var=$(IFS=',';echo "${vars[*]}";IFS=$' \t\n’)
###make json for parameter
echo '{"diseaseOMIMID": "MIM114480","miRTargetDB": "TargetScan", "miR2DiseaseDB":"miR2Disease", "backProb":0.5, "subnetWeight":0.5}'| jq --arg v "$var" '. + {"listOfmiRNAs":$v}’>para.json

curl -X POST --header 'Content-type: application/json' --header 'Accept: application/json' -d "@para.json" 'http://localhost:1234/RWRMTN/v1/rank' -o result.csv