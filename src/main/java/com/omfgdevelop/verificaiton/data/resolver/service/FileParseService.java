package com.omfgdevelop.verificaiton.data.resolver.service;

import com.omfgdevelop.verificaiton.data.resolver.dto.Inout;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class FileParseService {

    public List<Inout> processFile(List<Inout> list, byte[] bytes,String filename) throws IOException {


        String content = new String(bytes, StandardCharsets.UTF_8);

        list.add(Inout.builder().filename(filename).data(content).build());
        return list;
    }


}
