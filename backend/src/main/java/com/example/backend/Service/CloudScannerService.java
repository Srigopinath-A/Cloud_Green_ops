package com.example.backend.Service;

import java.util.List;

import com.example.backend.Model.CloudResource;
import com.example.backend.Model.CloudResourcer;

public interface CloudScannerService {

    List<CloudResourcer> scanAws();
    //List<CloudResource> scanAzure();
    List<CloudResource> scanGcp() throws Exception;

}
