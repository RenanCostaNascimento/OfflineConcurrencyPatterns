package edu.ifes.les.offlineconcurrencypatterns.controller;

import java.util.HashMap;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Renan
 */
public class LockManager {

    private static LockManager lockManager = null;

    /*O Integer corresponde ao identificador do recurso
     A String corresponde ao identificador do requisitante*/
    private final Map<Integer, String> readingLockTable = new HashMap<>();
    private final Map<Integer, String> writingLockTable = new HashMap<>();
    private final Map<String, Integer[]> coarseGrainedLockTable = new HashMap<>();

    private LockManager() {
    }

    /**
     * Busca a única instância do LockManager
     *
     * @return o LockManager
     */
    public static LockManager getInstance() {
        if (lockManager == null) {
            lockManager = new LockManager();
        }
        return lockManager;
    }

    /**
     * Tenta adquirir um lock de leitura
     *
     * @param idResource o id do recurso que se deseja ler
     * @param requester o requisitante da operação
     * @return true se o lock foi concedido
     */
    public boolean acquireReadingLock(Integer idResource, String requester) {
        if (writingLockTable.get(idResource) == null) {
            readingLockTable.put(idResource, requester);
            return true;
        }
        return false;
    }

    /**
     * Libera um lock de leitura.
     *
     * @param idResource o id do recurso que se deseja liberar
     * @param requester o requisitante da operação
     */
    public void releaseReadingLock(Integer idResource, String requester) {
        readingLockTable.remove(idResource, requester);
    }

    /**
     * Libera todos os locks de leitura de um determinando requisitante
     *
     * @param requester o requisitante que se deseja liberar os locks
     */
    public void releaseAllReadingLocks(String requester) {
        for (Map.Entry<Integer, String> entrySet : readingLockTable.entrySet()) {
            if (requester.equals(entrySet.getValue())) {
                readingLockTable.remove(entrySet.getKey());
            }
        }
    }

    /**
     * Adquiri um lock de escrita
     *
     * @param idResource o id do recurso que se deseja acessar
     * @param requester o requisitante da operação
     * @return true se o lock foi concedido
     */
    public boolean acquireWritingLock(Integer idResource, String requester) {
        if (writingLockTable.get(idResource) == null && readingLockTable.get(idResource) == null) {
            writingLockTable.put(idResource, requester);
            return true;
        }
        return false;
    }

    /**
     * Libera um lock de escrita
     *
     * @param idResource o id do recurso que se deseja liberar
     * @param requester o id do detentor do lock
     */
    public void releaseWritingLock(Integer idResource, String requester) {
        writingLockTable.remove(idResource, requester);
    }

    /**
     * Libera todos os locks de leitura de um requisitante
     *
     * @param requester o nome do requisitante
     */
    public void releaseAllWritingLocks(String requester) {
        for (Map.Entry<Integer, String> entrySet : writingLockTable.entrySet()) {
            if (requester.equals(entrySet.getValue())) {
                writingLockTable.remove(entrySet.getKey());
            }
        }
    }

    /**
     * Adquiri um lock coarse-grained para vários recursos solicitados por um
     * requisitante
     *
     * @param idResources os ids dos recursos que se deseja acessar
     * @param requester o requisitante da operação
     * @return true se o lock foi concedido;
     */
    public boolean acquireCoarseGrainedLock(Integer[] idResources, String requester) {

        for (Map.Entry<String, Integer[]> entrySet : coarseGrainedLockTable.entrySet()) {
            for (Integer lockedId : entrySet.getValue()) {
                if (checkArray(idResources, lockedId)) {
                    return false;
                }
            }
        }

        coarseGrainedLockTable.put(requester, idResources);
        return true;
    }

    /**
     * Libera um coarse-grained lock
     *
     * @param requester o requisitante que deseja liberar o lock.
     */
    public void releaseCoarseGrainedLock(String requester) {
        coarseGrainedLockTable.remove(requester);
    }

    public Map<Integer, String> getReadingLockTable() {
        return readingLockTable;
    }

    public Map<Integer, String> getWritingLockTable() {
        return writingLockTable;
    }

    /**
     * Verifica se existe um determinado valor em um array
     *
     * @param arrayToCheck o array que se deseja verificar
     * @param value o valor que se deseja encontrar
     * @return true se o valor estiver no array
     */
    private boolean checkArray(Integer[] arrayToCheck, Integer value) {
        for (Integer arrayValue : arrayToCheck) {
            if (value.equals(arrayValue)) {
                return true;
            }
        }

        return false;
    }

}
