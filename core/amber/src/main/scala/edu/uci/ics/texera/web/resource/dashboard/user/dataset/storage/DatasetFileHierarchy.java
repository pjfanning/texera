package edu.uci.ics.texera.web.resource.dashboard.user.dataset.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DatasetFileHierarchy {
  private final Map<String, Object> hierarchy;

  public DatasetFileHierarchy(String path) {
    this.hierarchy = this.scanDirectory(new File(path));
  }

  private Map<String, Object> scanDirectory(File dir) {
    Map<String, Object> hierarchy = new HashMap<>();
    File[] files = dir.listFiles();

    if (files == null || files.length == 0) {
      return hierarchy;
    }

    for (File file : files) {
      // Skip hidden files/directories
      if (file.getName().startsWith(".")) {
        continue;
      }

      if (file.isDirectory()) {
        hierarchy.put(file.getName(), scanDirectory(file));
      } else {
        hierarchy.put(file.getName(), "");  // You can replace this with anything proper.
      }
    }

    return hierarchy;
  }


  public Map<String, Object> getHierarchy() {
    return hierarchy;
  }

  public String toJSON() {
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.writeValueAsString(this.hierarchy);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      return "{}";
    }
  }
}

