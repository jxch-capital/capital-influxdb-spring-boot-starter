package org.jxch.capital.influx.registrar;

import org.jxch.capital.influx.config.InfluxRepositoryScan;
import org.jxch.capital.influx.repository.InfluxRepository;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class InfluxRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry, BeanNameGenerator importBeanNameGenerator) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(InfluxRepositoryScan.class.getName());
        String[] basePackages = (String[]) Objects.requireNonNull(attributes).get("basePackages");
        InfluxScanner binderBeanScan = new InfluxScanner(registry);
        binderBeanScan.addIncludeFilter((metadataReader, metadataReaderFactory) ->
                Arrays.stream(metadataReader.getClassMetadata().getInterfaceNames()).anyMatch(interfaceName ->
                        Objects.equals(interfaceName, InfluxRepository.class.getName())));
        binderBeanScan.scan(basePackages);
    }

}
