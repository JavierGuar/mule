/* 
 * $Header$
 * $Revision$
 * $Date$
 * ------------------------------------------------------------------------------------------------------
 * 
 * Copyright (c) Cubis Limited. All rights reserved.
 * http://www.cubis.co.uk 
 * 
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file. 
 *
 */
package org.mule.extras.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mule.config.ConfigurationException;
import org.mule.umo.model.ComponentNotFoundException;
import org.mule.umo.model.UMOContainerContext;
import org.mule.umo.model.ComponentResolverException;
import org.mule.util.ClassHelper;
import org.mule.extras.spring.config.ReaderInputStream;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.Reader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.Map;

/**
 * <code>SpringContainerContext</code> is a Spring Context that can expose spring-managed
 * components for use in the Mule framework.
 *
 * @author <a href="mailto:ross.mason@cubis.co.uk">Ross Mason</a>
 * @version $Revision$
 */
public class SpringContainerContext implements UMOContainerContext, BeanFactoryAware
{
    /**
     * logger used by this class
     */
    protected static transient Log logger = LogFactory.getLog(SpringContainerContext.class);

    /**
     * the application contect to use when resolving components
     */
    protected BeanFactory beanFactory;

    protected BeanFactory externalBeanFactory;

    protected String configFile;

    /**
     * Sets the spring application context used to build components
     *
     * @param beanFactory the context to use
     */
    public void setBeanFactory(BeanFactory beanFactory)
    {
        this.beanFactory = beanFactory;
    }

    public void setExternalBeanFactory(BeanFactory factory)
    {
        this.externalBeanFactory = factory;
    }

    /**
     * The spring application context used to build components
     *
     * @return spring application context
     */
    public BeanFactory getBeanFactory()
    {
        if(externalBeanFactory !=null) return externalBeanFactory;
        return beanFactory;
    }

    /* (non-Javadoc)
     * @see org.mule.umo.model.UMOContainerContext#getComponent(java.lang.Object)
     */
    public Object getComponent(Object key) throws ComponentNotFoundException
    {
        if (getBeanFactory() == null)
        {
            throw new IllegalStateException("Spring Application context has not been set");
        }
        if (key == null)
        {
            throw new ComponentNotFoundException("Component not found for null key");
        }

        if (key instanceof Class)
        {
            //We will assume that there should only be one object of
            //this class in the container for now
//            String[] names = getBeanFactory().getBeanDefinitionNames((Class) key);
//            if (names == null || names.length == 0 || names.length > 1)
//            {
                throw new ComponentNotFoundException("The container is unable to build single instance of " +
                        ((Class) key).getName() + " number of instances found was: 0");
//            }
//            else
//            {
//                key = names[0];
//            }
        }
        try
        {
            return getBeanFactory().getBean(key.toString());
        }
        catch (BeansException e)
        {
            throw new ComponentNotFoundException("Component not found for key: " + key.toString(), e);
        }
    }

    public String getConfigFile()
    {
        return configFile;
    }

    /**
     * @param configFile The configFile to set.
     */
    public void setConfigFile(String configFile) throws ConfigurationException
    {
        this.configFile = configFile;
        try
        {
            if(ClassHelper.getResource(configFile, getClass())==null) {
                logger.warn("Spring config resource: " + configFile + " not found on class path, attempting to load it from local file");
                setExternalBeanFactory(new FileSystemXmlApplicationContext(configFile));
            } else {
                logger.info("Loading Spring config from classpath, resource is: " + configFile);
                setExternalBeanFactory(new ClassPathXmlApplicationContext(configFile));
            }
        } catch (BeansException e)
        {
            throw new ConfigurationException("Failed to load Application Context: " + e.getMessage(), e);
        }
    }

    public void configure(Reader configuration, Map configurationProperties) throws ComponentResolverException {
        BeanFactory bf = new XmlBeanFactory(new ReaderInputStream(configuration));
    }
}
