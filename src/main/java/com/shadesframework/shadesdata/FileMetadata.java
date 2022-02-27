package com.shadesframework.shadesdata;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class FileMetadata implements Metadata {
    
    private HashMap<String, DataSet> dataSets = new HashMap();

    @Override
    public ArrayList<DataSet> getConfiguredDataSets() throws Exception {
        ArrayList<DataSet> dataSetList = new ArrayList();
        Pattern pattern = Pattern.compile(".*synth");
        Collection<String> synthFiles = FileHelper.getResources(pattern);
        
        for (String synthFile : synthFiles) {
            String fileContent = FileHelper.readFileContent(synthFile);
            Map synthMeta = getJsonMap(fileContent);
            HashMap columns = (HashMap)synthMeta.get("metaset");
            HashMap relatedSets = getRelatedSets((HashMap)synthMeta);
            HashMap storage = (HashMap)synthMeta.get("storage");
            Storage storageHelper = getStorage(storage);

            String dataSetName = FileHelper.getFileNameFromFullPath(synthFile);

            // avoid duplicate creation of dataset
            if (!dataSets.keySet().contains(dataSetName)) {
                DataSet dataSet = new DataSet(dataSetName,storageHelper,this,columns,relatedSets,storage);
                dataSets.put(dataSetName, dataSet);
                dataSetList.add(dataSet);
            } else {
                DataSet dataSet = dataSets.get(dataSetName);
                dataSetList.add(dataSet);
            }
        }
        return dataSetList;
    }

    @Override
    public DataSet getDataSet(String dataSetName) {
        return dataSets.get(dataSetName);
    }

    public Map getJsonMap(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map map = mapper.readValue(json, Map.class);
        return map;
    }

    private HashMap getRelatedSets(HashMap synthMap) throws Exception {
        HashMap relations = (HashMap)synthMap.get("metarelations");
        return relations;
    }

    private Storage getStorage(HashMap storageMeta) throws Exception {
        if (storageMeta == null) {
            throw new Exception("storage metadata cannot be null");
        }
        String storageType = (String)storageMeta.get("type");
        String pointer = (String)storageMeta.get("pointer");

        if (storageType==null) {
            throw new Exception("storage 'type' cannot be null");
        }
        if (storageType.trim().toLowerCase().equals("csv")) {
            return new CsvFileStorage(pointer);
        }
        return null;
    }
    
}