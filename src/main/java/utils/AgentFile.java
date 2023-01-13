package utils;


import org.checkerframework.checker.units.qual.A;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AgentFile {

    private final static String agent_config_path = System.getProperty("user.dir") + "/" + "agentconfig/agent.yaml";

    public static List<String> getAgentClass() throws FileNotFoundException {
        return readYamlList("agent-calss");
    }


    public static List<String> readYamlList(String prefix) throws FileNotFoundException {
        InputStream input =
                new FileInputStream(agent_config_path);
        Yaml yaml = new Yaml();
        LinkedHashMap<String, Object> list = yaml.load(input);
        return (List<String>) list.get(prefix);
    }


}
