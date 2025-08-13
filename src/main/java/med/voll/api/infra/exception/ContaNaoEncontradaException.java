package med.voll.api.infra.exception;


public class ContaNaoEncontradaException extends RuntimeException {
    public ContaNaoEncontradaException() {
        super("Usuário não encontrado ou conta inativa");
    }
}

