/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ifes.les.offlineconcurrencypatterns.main;

import edu.ifes.les.offlineconcurrencypatterns.controller.LockManager;
import edu.ifes.les.offlineconcurrencypatterns.model.Client;
import edu.ifes.les.offlineconcurrencypatterns.persistence.ClientDao;
import java.util.List;

/**
 *
 * @author Renan
 */
public class Main {

    public static void main(String args[]) throws Exception {

        //optimistic();
        //pessimistic();
        coarseGrained();

    }

    public static void optimistic() throws Exception {

        /*CASO NORMAL*/
        ClientDao clienteDao = new ClientDao();

        Client cliente1 = clienteDao.get(1);
        Client cliente2 = clienteDao.get(1);

        cliente1.setNome("Um Nome");
        cliente2.setNome("Outro Nome");

        clienteDao.update(cliente1);
        clienteDao.update(cliente2);

        System.out.println(clienteDao.get(1));

        /*CASO OTIMISTA*/
        cliente1.setNome("Mais Um Nome");
        cliente2.setNome("Mais Outro Nome");

        System.out.println("Linhas afetadas pelo primeiro update " + clienteDao.optimisticUpdate(cliente1));
        System.out.println("Linhas afetadas pelo segundo update " + clienteDao.optimisticUpdate(cliente2));

        System.out.println(clienteDao.get(1));

    }

    public static void pessimistic() throws Exception {

        /*READS CONCORRENTES IMPOSSIBILITANDO WRITES*/
        ClientDao clienteDao = new ClientDao();
        LockManager lockManager = LockManager.getInstance();

        /*read concorrente*/
        Client cliente1 = clienteDao.pessimisticRead(1, "User1");
        Client cliente2 = clienteDao.pessimisticRead(1, "User2");

        System.out.println("User1 " + cliente1);
        System.out.println("User2 " + cliente2);

        /*write do User1 bloqueado devido ao read lock do User2*/
        lockManager.releaseReadingLock(1, "User1");
        cliente1.setNome("Nome Novo");
        System.out.println("Linhas afetadas pelo Cliente 1: " + clienteDao.pessimisticWrite(cliente1, "User1"));

        /*release do read lock do User2 permite o write do User1*/
        lockManager.releaseReadingLock(1, "User2");
        System.out.println("Linhas afetadas pelo Cliente 1: " + clienteDao.pessimisticWrite(cliente1, "User1"));
        lockManager.releaseWritingLock(1, "User1");

        /*WRITES IMPOSSIBILITANDO READS*/
        clienteDao.pessimisticWrite(cliente1, "User1");
        cliente2 = clienteDao.pessimisticRead(1, "User2");
        if(cliente2 == null){
            System.out.println("Não foi possível buscar o cliente para o User2 devido ao write lock de User1");
        }
        
        /*User1 dá release no write lock*/
        lockManager.releaseWritingLock(1, "User1");
        cliente2 = clienteDao.pessimisticRead(1, "User2");
        System.out.println("Agora User2 conseguiu o read lock: " + cliente2);
        lockManager.releaseReadingLock(1, "User2");

    }
    
    public static void coarseGrained() throws Exception{
        
        /*User1 locka dois recursos da tabela*/
        ClientDao clienteDao = new ClientDao();
        LockManager lockManager = LockManager.getInstance();
        
        Integer[] idResouces1 = {1, 2};
        List<Client> clients1 = clienteDao.coarseGrainedRead(idResouces1, "User1");
        
        for (Client client : clients1) {
            System.out.println(client);
        }
        
        /*User2 tentar dar lock em um dos recursos lockados por User1 e não consegue*/
        Integer[] idResouces2 = {1, 3};
        List<Client> clients2 = clienteDao.coarseGrainedRead(idResouces2, "User2");
        
        if(clients2 == null){
            System.out.println("User2 não conseguiu os recursos lockados por User1.");
        }
        
        /*User1 libera os locks e User2 consegue acessar*/
        lockManager.releaseCoarseGrainedLock("User1");
        
        clients2 = clienteDao.coarseGrainedRead(idResouces2, "User2");
        
        for (Client client : clients2) {
            System.out.println(client);
        }
    }

}
