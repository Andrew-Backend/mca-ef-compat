package forge.net.mca.entity.ai.chatAI.inworldAIModules.api;

public record Session(String name, Session.SessionCharacter[] sessionCharacters, String loadedScene) {
   public record SessionCharacter(String name, String character, String displayName, Session.SessionCharacter.CharacterAssets characterAssets) {
      public record CharacterAssets(String avatarImg, String avatarImgOptional) {
      }
   }
}
