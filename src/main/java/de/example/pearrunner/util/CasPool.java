/**
 *
 */
package de.example.pearrunner.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.example.pearrunner.initializers.PearHandler;
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

	private PearHandler pearHandler;


	@Autowired
	public CasPool(PearHandler pearHandler) {

		this.pearHandler = pearHandler;
		this.casPool = new LinkedBlockingQueue<>(CasPool.CAS_POOL_SIZE);

	}


	private void addCleanCasToPool(int indexOfCas) {

		try {

			JCas newJCas = this.pearHandler.getAE().getResourceManager().getCasManager().getCas(PearHandler.POOLISIOUS).getJCas();

			this.casPool.add(newJCas);
		} catch (CASException e) {
			CasPool.log.error("Could not create a cas", e);
			throw new RuntimeException(e);
		}
	}


	public JCas getCas() {

		try {
			return this.casPool.poll(CasPool.MAX_WAIT_FOR_CAS_IN_SEK, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			CasPool.log.error("Could not get a clean cas in " + CasPool.MAX_WAIT_FOR_CAS_IN_SEK + " sec", e);
			throw new RuntimeException(e);
		}
	}


	public void returnCas(JCas returnToPool) {

		returnToPool.reset();
		this.casPool.add(returnToPool);
	}


	public void initPool() {

		IntStream.of(CasPool.CAS_POOL_SIZE).forEach(this::addCleanCasToPool);
	}

}
