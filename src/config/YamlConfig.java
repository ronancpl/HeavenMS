package config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;


public class YamlConfig {

    public static final YamlConfig config = fromFile("config.yaml");
    private List<WorldConfig> worlds;
    private ServerConfig server;

    public static YamlConfig fromFile(String filename) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            YamlConfig yamlConfig = mapper.readValue(new File(filename), YamlConfig.class);
        } catch (FileNotFoundException e) {
            String message = "Could not read config file " + filename + ": " + e.getMessage();
            throw new RuntimeException(message);
        }
    }

    public List<WorldConfig> getWorldConfigs() {
        return this.worlds;
    }

    public config.ServerConfig getServer() {
        return server;
    }
}
