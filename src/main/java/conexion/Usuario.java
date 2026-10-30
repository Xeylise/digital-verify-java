
package conexion;

import java.time.LocalDate;

public class Usuario {

    private int idUser;
    private String nicknameUser;
    private String emailUser;
    private String passwordUser;
    private LocalDate dateCreate;
    private String statusUserAccount;

    public Usuario(int idUser, String nicknameUser, String emailUser, String passwordUser, LocalDate dateCreate, String statusUserAccount) {
        this.idUser = idUser;
        this.nicknameUser = nicknameUser;
        this.emailUser = emailUser;
        this.passwordUser = passwordUser;
        this.dateCreate = dateCreate;
        this.statusUserAccount = statusUserAccount;
    }

    public int getIdUser() {
        return idUser;
    }

    public String getNicknameUser() {
        return nicknameUser;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public String getPasswordUser() {
        return passwordUser;
    }

    public LocalDate getDateCreate() {
        return dateCreate;
    }

    public String getStatusUserAccount() {
        return statusUserAccount;
    }
}

