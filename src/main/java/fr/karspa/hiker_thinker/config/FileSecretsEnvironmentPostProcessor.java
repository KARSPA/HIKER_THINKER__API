package fr.karspa.hiker_thinker.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileSecretsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String FILE_SUFFIX = "_FILE";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        Map<String, Object> fileSecrets = new HashMap<>();

        // Parcourir les variables d'environnement du système
        Map<String, String> envVars = System.getenv();
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith(FILE_SUFFIX)) {
                // Retirer le suffixe _FILE pour obtenir le nom de la propriété à injecter
                String propertyKey = key.substring(0, key.length() - FILE_SUFFIX.length());
                String filePath = entry.getValue();
                try {
                    // Lire le contenu du fichier et le nettoyer (trim)
                    String secretValue = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8).trim();
                    fileSecrets.put(propertyKey, secretValue);
                } catch (IOException e) {
                    System.err.println("Erreur lors de la lecture du secret pour " + key + " depuis " + filePath + ": " + e.getMessage());
                }
            }
        }

        // Ajouter ces propriétés avec une priorité élevée
        if (!fileSecrets.isEmpty()) {
            propertySources.addFirst(new MapPropertySource("fileSecrets", fileSecrets));
        }
    }
}