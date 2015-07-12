/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ifes.les.offlineconcurrencypatterns.model;

/**
 *
 * @author 20121bsi0252
 */
public class Client {
    private Integer id;
    private String nome;
    private String sexo;
    private String email;
    private int versao;

    public Client(String nome, String sexo, String email, int versao) {
        this.nome = nome;
        this.sexo = sexo;
        this.email = email;
        this.versao = versao;
    }
    
    @Override
    public String toString(){
        return this.nome + ", " + this.versao;
    }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getVersao() {
        return versao;
    }

    public void setVersao(int versao) {
        this.versao = versao;
    }
    
    public void incrementarVersao(){
        this.versao += 1;
    }
    
}
