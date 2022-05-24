package ru.pulsar.jenkins.library.configuration

import com.cloudbees.groovy.cps.NonCPS
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonPropertyDescription

@JsonIgnoreProperties(ignoreUnknown = true)
class EdtValidateOptions implements Serializable {

    @JsonPropertyDescription("""Путь к конфигурационному файлу проверок EDT.
    По умолчанию содержит значение "./tools/ManagedEnvironments.prefs".
    """)
    String managedEnvironmentsFile = "./tools/ManagedEnvironments.prefs"

    @Override
    @NonCPS
    String toString() {
        return "EdtValidateOptions{" +
            "managedEnvironmentsFile='" + managedEnvironmentsFile + '\'' +
            '}';
    }
}
