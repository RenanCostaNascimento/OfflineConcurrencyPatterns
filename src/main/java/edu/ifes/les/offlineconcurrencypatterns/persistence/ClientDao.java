/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ifes.les.offlineconcurrencypatterns.persistence;

import edu.ifes.les.offlineconcurrencypatterns.controller.LockManager;
import edu.ifes.les.offlineconcurrencypatterns.model.Client;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ClientDao extends Dao {

    /**
     * recupera um cliente do banco
     *
     * @param idClient o id do cliente que se deseja retornar
     * @return o cliente com o id informado, se existir. Null do contrário
     * @throws Exception
     */
    public Client get(int idClient) throws Exception {

        open();

        stmt = con.prepareStatement("SELECT * FROM cliente WHERE id = ?");
        stmt.setInt(1, idClient);
        stmt.execute();

        ResultSet resultSet = stmt.getResultSet();
        if (resultSet != null && resultSet.next()) {
            String nome = resultSet.getString("nome");
            String sexo = resultSet.getString("sexo");
            String email = resultSet.getString("email");
            int versao = resultSet.getInt("versao");

            Client cliente = new Client(nome, sexo, email, versao);
            cliente.setId(resultSet.getInt("id"));
            close();

            return cliente;
        }

        close();
        return null;

    }

    /**
     * Atualiza um cliente.
     *
     * @param client o cliente que se deseja atualizar
     * @return a quantidade de linhas afetadas
     * @throws Exception
     */
    public int update(Client client) throws Exception {

        open();

        int registrosAfetados;

        stmt = con.prepareStatement("UPDATE cliente SET nome = ?, sexo = ?, email = ?, versao = ? WHERE id = ?");
        stmt.setString(1, client.getNome());
        stmt.setString(2, client.getSexo());
        stmt.setString(3, client.getEmail());
        stmt.setInt(4, client.getVersao());
        stmt.setInt(5, client.getId());

        registrosAfetados = stmt.executeUpdate();
        close();

        return registrosAfetados;

    }

    /**
     * Atualiza um cliente, se possível. Leva em consideração a versão do cliente
     * que se desesa salvar (Optimistic Offline Lock)
     *
     * @param client
     * @return
     * @throws Exception
     */
    public int optimisticUpdate(Client client) throws Exception {

        open();

        int registrosAfetados;
        int versao = client.getVersao();
        client.incrementarVersao();

        stmt = con.prepareStatement("UPDATE cliente SET nome = ?, sexo = ?, email = ?, versao = ? WHERE id = ? AND versao = ?");
        stmt.setString(1, client.getNome());
        stmt.setString(2, client.getSexo());
        stmt.setString(3, client.getEmail());
        stmt.setInt(4, client.getVersao());
        stmt.setInt(5, client.getId());
        stmt.setInt(6, versao);

        registrosAfetados = stmt.executeUpdate();
        close();

        return registrosAfetados;
    }

    /**
     * Realiza leitura pessimista.
     *
     * @param idCliente id do cliente que se deseja ler
     * @param requester nome do requisitante
     * @return o Cliente encontrado, ou null se não foi possível encontrá-lo
     * @throws Exception
     */
    public Client pessimisticRead(int idCliente, String requester) throws Exception {
        LockManager lockManager = LockManager.getInstance();
        if (lockManager.acquireReadingLock(idCliente, requester)) {
            Client cliente = get(idCliente);
            return cliente;
        }
        return null;
    }

    /**
     * Realiza escrita pessimista
     *
     * @param cliente o cliente que se deseja atualizar
     * @param requester o requisitante da operação
     * @return a quantidade de linhas afetadas pela query do banco
     * @throws Exception
     */
    public int pessimisticWrite(Client cliente, String requester) throws Exception {
        LockManager lockManager = LockManager.getInstance();
        if (lockManager.acquireWritingLock(cliente.getId(), requester)) {
            int rowsAffected = update(cliente);
            return rowsAffected;
        }
        return 0;
    }

    /**
     * Realiza uma leitura coarseGrained
     *
     * @param idResources os ids dos recursos que se deseja lockar.
     * @param requester o requisitante da operação
     * @return uma lista com o clientes encontrados, ou null caso não tenha sido
     * possível encontrar nenhuma cliente.
     * @throws Exception
     */
    public List<Client> coarseGrainedRead(Integer[] idResources, String requester) throws Exception {

        LockManager lockManager = LockManager.getInstance();
        List<Client> clients;

        if (lockManager.acquireCoarseGrainedLock(idResources, requester)) {
            clients = new ArrayList<>();

            for (Integer id : idResources) {
                clients.add(get(id));
            }

            return clients;
        }

        return null;
    }
}
