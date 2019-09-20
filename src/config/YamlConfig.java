package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;


public class YamlConfig {

    public static final YamlConfig config = fromFile("config.yaml");
    public List<WorldConfig> worlds;
    public ServerConfig server;

    public static YamlConfig fromFile(String filename) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            return mapper.readValue(new File(filename), YamlConfig.class);
        } catch (FileNotFoundException e) {
            String message = "Could not read config file " + filename + ": " + e.getMessage();
            throw new RuntimeException(message);
        } catch (IOException e) {
            String message = "Could not successfully parse config file " + filename + ": " + e.getMessage();
            throw new RuntimeException(message);
        }
    }
}
