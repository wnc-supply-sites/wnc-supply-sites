package com.vanatta.helene.needs.loader;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.List;

public class NeedsCsvLoader {
  public static List<CsvDistroData> readFile(String inputFileName) {
    try {
      var url = NeedsCsvLoader.class.getResource(inputFileName);
      if (url == null) {
        throw new RuntimeException("File not found: " + inputFileName);
      }

      var uri = url.toURI();
      File f = new File(uri);
      return new CsvToBeanBuilder(new FileReader(f)).withType(CsvDistroData.class).build().parse();

    } catch (FileNotFoundException | URISyntaxException e) {
      throw new RuntimeException("Unable to find file: " + inputFileName, e);
    }
  }
}
