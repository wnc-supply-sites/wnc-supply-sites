package com.vanatta.helene.supply.loader;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Reads site-supply CSV file. It contains site-name and 'items'. Items shoudl be a comma delimited
 * list.
 */
public class SupplyCsvReader {
  public static List<SupplyData> read(String inputFileName) {

    try {
      var url = SupplyCsvReader.class.getResource(inputFileName);
      if (url == null) {
        throw new RuntimeException("File not found: " + inputFileName);
      }

      var uri = url.toURI();
      File f = new File(uri);
      return new CsvToBeanBuilder(new FileReader(f)).withType(SupplyData.class).build().parse();

    } catch (FileNotFoundException | URISyntaxException e) {
      throw new RuntimeException("Unable to find file: " + inputFileName, e);
    }
  }
}
