package com.example.backend.Service;

import java.util.List;

import com.example.backend.Model.CloudResource;

public interface CloudScannerService {

    //List<CloudResource> scanAws();
    //List<CloudResource> scanAzure();
    List<CloudResource> scanGcp() throws Exception;

}
