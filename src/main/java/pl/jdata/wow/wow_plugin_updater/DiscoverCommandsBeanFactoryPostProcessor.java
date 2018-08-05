package pl.jdata.wow.wow_plugin_updater;

import java.util.Objects;
import java.util.stream.Stream;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

@Component
public class DiscoverCommandsBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        final String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();

        Stream.of(beanDefinitionNames).forEach((String beanName) -> processBeanDefinition(beanFactory, beanName));
    }

    private void processBeanDefinition(ConfigurableListableBeanFactory beanFactory, String beanName) {
        Objects.requireNonNull(beanName);

        final BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);

        //        final String beanClassName = beanDefinition.getBeanClassName();
        //
        //        try {
        //            final Class<?> beanClass = Class.forName(beanClassName);
        //        } catch (Throwable e) {
        //            throw new RuntimeException("Error when processing bean " + beanName, e);
        //        }
    }

}
