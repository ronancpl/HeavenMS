package constants;

import client.MapleCharacter;

/**
 *
 * @author Drago - Dragohe4rt
 */
public class LanguageConstants {
    
    public static String CPQBlue;
    public static String CPQError;
    public static String CPQEntry;
    public static String CPQFindError;
    public static String CPQRed;
    public static String CPQPlayerExit;
    public static String CPQEntryLobby;
    public static String CPQPickRoom;
    public static String CPQExtendTime;
    public static String CPQLeaderNotFound;
    public static String CPQChallengeRoomAnswer;
    public static String CPQChallengeRoomSent;

    public static LanguageConstants Languages(MapleCharacter chr) {
        if (chr.getLanguage() == 0) {
            LanguageConstants.CPQBlue = "Maple Azul";
            LanguageConstants.CPQRed = "Maple Vermelho";
            LanguageConstants.CPQExtendTime = "O tempo foi estendido.";
            LanguageConstants.CPQPlayerExit = " deixou o Carnaval de Monstros.";
            LanguageConstants.CPQError = "Ocorreu um problema. Favor recriar a sala.";
            LanguageConstants.CPQLeaderNotFound = "Nao foi possivel encontrar o Lider.";
            LanguageConstants.CPQPickRoom = "Inscreva-se no Festival de Monstros!\r\n";            
            LanguageConstants.CPQChallengeRoomAnswer = "O grupo esta respondendo um desafio no momento.";
            LanguageConstants.CPQChallengeRoomSent = "Um desafio foi enviado para o grupo na sala. Aguarde um momento.";
            LanguageConstants.CPQFindError = "Nao foi possivel encontrar um grupo nesta sala.\r\nProvavelmente o grupo foi desfeito dentro da sala!";
            LanguageConstants.CPQEntryLobby = "Agora voce ira receber desafios de outros grupos. Se voce nao aceitar um desafio em 3 minutos, voce sera levado para fora.";
            LanguageConstants.CPQEntry = "Voce pode selecionar \"Invocar Monstros\", \"Habilidade\", ou \"Protetor\" como sua tatica durante o Carnaval dos Monstros. Use Tab a F1~F12 para acesso rapido!";

            
            
        } else if (chr.getLanguage() == 1) {
            LanguageConstants.CPQBlue = "Maple Azul";
            LanguageConstants.CPQRed = "Maple Rojo";
            LanguageConstants.CPQExtendTime = "El tiempo se ha ampliado.";
            LanguageConstants.CPQPlayerExit = " ha dejado el Carnaval de Monstruos.";
            LanguageConstants.CPQLeaderNotFound = "No se pudo encontrar el Lider.";
            LanguageConstants.CPQPickRoom = "!Inscribete en el Festival de Monstruos!\r\n";
            LanguageConstants.CPQError = "Se ha producido un problema. Por favor, volver a crear una sala.";
            LanguageConstants.CPQChallengeRoomAnswer = "El grupo esta respondiendo un desafio en el momento.";
            LanguageConstants.CPQChallengeRoomSent = "Un desafio fue enviado al grupo en la sala. Espera un momento.";
            LanguageConstants.CPQFindError = "No se pudo encontrar un grupo en esta sala.\r\nProbablemente el grupo fue deshecho dentro de la sala!";
            LanguageConstants.CPQEntryLobby = "Ahora usted recibira los retos de otros grupos. Si usted no acepta un desafio en 3 minutos, usted sera llevado hacia fuera.";
            LanguageConstants.CPQEntry = "Usted puede seleccionar \"Invocar Monstruos \", \"Habilidad \", o \"Protector \" como su tactica durante el Carnaval de los Monstruos. Utilice Tab y F1 ~ F12 para acceso rapido!";

            
        } else if (chr.getLanguage() == 2) {
            LanguageConstants.CPQBlue = "Maple Blue";
            LanguageConstants.CPQRed = "Maple Red";
            LanguageConstants.CPQPlayerExit = " left the Carnival of Monsters.";
            LanguageConstants.CPQExtendTime = "The time has been extended.";
            LanguageConstants.CPQLeaderNotFound = "Could not find the Leader.";
            LanguageConstants.CPQError = "There was a problem. Please re-create a room.";
            LanguageConstants.CPQPickRoom = "Sign up for the Monster Festival!\r\n";
            LanguageConstants.CPQChallengeRoomAnswer = "The group is currently facing a challenge.";
            LanguageConstants.CPQChallengeRoomSent = "A challenge has been sent to the group in the room. Please wait a while.";
            LanguageConstants.CPQFindError = "We could not find a group in this room.\r\nProbably the group was scrapped inside the room!";
            LanguageConstants.CPQEntryLobby = "You will now receive challenges from other groups. If you do not accept a challenge within 3 minutes, you will be taken out.";
            LanguageConstants.CPQEntry = "You can select \"Summon Monsters \", \"Ability \", or \"Protector \" as your tactic during the Monster Carnival. Use Tab and F1 ~ F12 for quick access!";
            
        }
        return null;
    }
}
