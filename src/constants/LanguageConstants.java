package constants;

import client.MapleCharacter;

/**
 *
 * @author Drago - Dragohe4rt
 */
public class LanguageConstants {
	// Portugues
    public static String CPQAzul;
    public static String CPQErro;
    public static String CPQEntrada;
    public static String CPQEscolha;
    public static String CPQVermelho;
    public static String CPQPlayerExit;
    public static String CPQEntradaLobby;
    public static String CPQInicioEscolha;
    public static String CPQTempoExtendido;
    public static String CPQLiderNaoEncontrado;
    public static String CPQInicioEscolhaEmEscolha;
    public static String CPQInicioEscolhaEnviada;

    public static LanguageConstants Linguas(MapleCharacter chr) {
        if (chr.getLingua() == 0) {
            LanguageConstants.CPQAzul = "Maple Azul";
            LanguageConstants.CPQVermelho = "Maple Vermelho";
            LanguageConstants.CPQTempoExtendido = "O tempo foi estendido.";
            LanguageConstants.CPQPlayerExit = " deixou o Carnaval de Monstros.";
            LanguageConstants.CPQErro = "Ocorreu um problema. Favor recriar a sala.";
            LanguageConstants.CPQLiderNaoEncontrado = "Nao foi possivel encontrar o Lider.";
            LanguageConstants.CPQInicioEscolha = "Inscreva-se no Festival de Monstros!\\r\\n";            
            LanguageConstants.CPQInicioEscolhaEmEscolha = "O grupo esta respondendo um desafio no momento.";
            LanguageConstants.CPQInicioEscolhaEnviada = "Um desafio foi enviado para o grupo na sala. Aguarde um momento.";
            LanguageConstants.CPQEscolha = "Nao foi possivel encontrar um grupo nesta sala.\\r\\nProvavelmente o grupo foi desfeito dentro da sala!";
            LanguageConstants.CPQEntradaLobby = "Agora voce ira receber desafios de outros grupos. Se voce nao aceitar um desafio em 3 minutos, voce sera levado para fora.";
            LanguageConstants.CPQEntrada = "Voce pode selecionar \"Invocar Monstros\", \"Habilidade\", ou \"Protetor\" como sua tatica durante o Carnaval dos Monstros. Use Tab a F1~F12 para acesso rapido!";

            
            
        } else if (chr.getLingua() == 1) {
            LanguageConstants.CPQAzul = "Maple Azul";
            LanguageConstants.CPQVermelho = "Maple Rojo";
            LanguageConstants.CPQTempoExtendido = "El tiempo se ha ampliado.";
            LanguageConstants.CPQPlayerExit = " ha dejado el Carnaval de Monstruos.";
            LanguageConstants.CPQLiderNaoEncontrado = "No se pudo encontrar el Lider.";
            LanguageConstants.CPQInicioEscolha = "!Inscribete en el Festival de Monstruos!\\r\\n";
            LanguageConstants.CPQErro = "Se ha producido un problema. Por favor, volver a crear una sala.";
            LanguageConstants.CPQInicioEscolhaEmEscolha = "El grupo esta respondiendo un desafio en el momento.";
            LanguageConstants.CPQInicioEscolhaEnviada = "Un desafio fue enviado al grupo en la sala. Espera un momento.";
            LanguageConstants.CPQEscolha = "No se pudo encontrar un grupo en esta sala.\\r\\nProbablemente el grupo fue deshecho dentro de la sala!";
            LanguageConstants.CPQEntradaLobby = "Ahora usted recibira los retos de otros grupos. Si usted no acepta un desafio en 3 minutos, usted sera llevado hacia fuera.";
            LanguageConstants.CPQEntrada = "Usted puede seleccionar \"Invocar Monstruos \", \"Habilidad \", o \"Protector \" como su tactica durante el Carnaval de los Monstruos. Utilice Tab y F1 ~ F12 para acceso rapido!";

            
        } else if (chr.getLingua() == 2) {
            LanguageConstants.CPQAzul = "Maple Blue";
            LanguageConstants.CPQVermelho = "Maple Red";
            LanguageConstants.CPQPlayerExit = " left the Carnival of Monsters.";
            LanguageConstants.CPQTempoExtendido = "The time has been extended.";
            LanguageConstants.CPQLiderNaoEncontrado = "Could not find the Leader.";
            LanguageConstants.CPQErro = "There was a problem. Please re-create a room.";
            LanguageConstants.CPQInicioEscolha = "Sign up for the Monster Festival!\\r\\n";
            LanguageConstants.CPQInicioEscolhaEmEscolha = "The group is currently facing a challenge.";
            LanguageConstants.CPQInicioEscolhaEnviada = "A challenge has been sent to the group in the room. Please wait a while.";
            LanguageConstants.CPQEscolha = "We could not find a group in this room.\\r\\nProbably the group was scrapped inside the room!";
            LanguageConstants.CPQEntradaLobby = "You will now receive challenges from other groups. If you do not accept a challenge within 3 minutes, you will be taken out.";
            LanguageConstants.CPQEntrada = "You can select \"Summon Monsters \", \"Ability \", or \"Protector \" as your tactic during the Monster Carnival. Use Tab and F1 ~ F12 for quick access!";
            
        }
        return null;
    }
}
