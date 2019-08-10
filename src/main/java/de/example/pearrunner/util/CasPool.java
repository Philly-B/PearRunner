/**
 *
 */
package de.example.pearrunner.util;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.CasCreationUtils;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author phil
 *
 */
@Component
@Slf4j
public class CasPool {

    public static final int CAS_POOL_SIZE = 5;
    public static final int MAX_WAIT_FOR_CAS_IN_SEK = 10;

    private BlockingQueue<JCas> casPool;

    public CasPool() {

	casPool = new LinkedBlockingQueue<>(CAS_POOL_SIZE);

	IntStream.of(CAS_POOL_SIZE).forEach(this::addCleanCasToPool);
    }

    private void addCleanCasToPool(int indexOfCas) {

	try {
	    casPool.add(CasCreationUtils.createCas(new ArrayList<>()).getJCas());
	} catch (CASException | ResourceInitializationException e) {
	    log.error("Could not create a cas", e);
	    throw new RuntimeException(e);
	}
    }

    public JCas getCas() {
	try {
	    return casPool.poll(MAX_WAIT_FOR_CAS_IN_SEK, TimeUnit.SECONDS);
	} catch (InterruptedException e) {
	    log.error("Could not get a clean cas in " + MAX_WAIT_FOR_CAS_IN_SEK + " sec", e);
	    throw new RuntimeException(e);
	}
    }

    public void returnCas(JCas returnToPool) {

	returnToPool.reset();
	casPool.add(returnToPool);
    }

}
