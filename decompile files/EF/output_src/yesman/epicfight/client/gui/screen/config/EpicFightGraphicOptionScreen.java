package yesman.epicfight.client.gui.screen.config;

import java.io.File;
import java.io.IOException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisableState;
import yesman.epicfight.client.camera.EpicFightTpsCameraDisabledReason;
import yesman.epicfight.client.gui.datapack.screen.MessageScreen;
import yesman.epicfight.client.gui.widgets.ColorSlider;
import yesman.epicfight.client.gui.widgets.EpicFightOptionList;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightGraphicOptionScreen extends EpicFightOptionSubScreen {
   private EpicFightOptionList optionsList;

   public EpicFightGraphicOptionScreen(Screen parentScreen) {
      super(parentScreen, Component.m_237115_(EpicFightMod.format("gui.%s.graphic_options")));
   }

   @Override
   protected void m_7856_() {
      super.m_7856_();
      this.optionsList = new EpicFightOptionList(this.f_96541_, this.f_96543_, this.f_96544_, 32, this.f_96544_ - 32, 25);
      int buttonHeight = -32;
      Button showTargetIndicatorButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.target_indicator." + (ClientConfig.showTargetIndicator ? "on" : "off"))), button -> {
               ClientConfig.showTargetIndicator = !ClientConfig.showTargetIndicator;
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.target_indicator." + (ClientConfig.showTargetIndicator ? "on" : "off"))));
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 - 8)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.target_indicator.tooltip"))))
         .m_253136_();
      Button healthBarVisibilityOptionButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.health_bar_show_option." + ClientConfig.healthBarVisibility.m_7912_())), button -> {
               ClientConfig.healthBarVisibility = ClientConfig.healthBarVisibility.nextEnum();
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.health_bar_show_option." + ClientConfig.healthBarVisibility.m_7912_())));
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 - 8)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.health_bar_show_option.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(showTargetIndicatorButton, healthBarVisibilityOptionButton);
      buttonHeight += 24;
      Button cameraSetupButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.tps_setup")),
            button -> {
               if (Minecraft.m_91087_().f_91073_ != null && Minecraft.m_91087_().f_91074_ != null) {
                  Minecraft.m_91087_().m_91152_(new TPSSettingScreen(this));
               } else {
                  Minecraft.m_91087_()
                     .m_91152_(
                        new MessageScreen(
                              "Warning",
                              "You can open camera setup screen only after entering the world",
                              this,
                              button2 -> Minecraft.m_91087_().m_91152_(this),
                              300,
                              70
                           )
                           .autoCalculateHeight()
                     );
               }
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 - 8)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.tps_setup.tooltip"))))
         .m_253136_();
      Button cameraTypeButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.tps_perspective." + ParseUtil.toLowerCase(ClientConfig.getCameraMode().name()))), button -> {
               ClientConfig.cameraMode = ClientConfig.getCameraMode().nextEnum();
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.tps_perspective." + ParseUtil.toLowerCase(ClientConfig.getCameraMode().name()))));
               cameraSetupButton.f_93623_ = ClientConfig.getCameraMode().hasTPSTransition();
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.tps_perspective.tooltip"))))
         .m_253136_();
      cameraSetupButton.f_93623_ = ClientConfig.getCameraMode().hasTPSTransition();
      this.optionsList.addSmall(cameraTypeButton, cameraSetupButton);
      buttonHeight += 24;
      Button bloodEffectsButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.blood_effects." + (ClientConfig.bloodEffects ? "on" : "off"))), button -> {
               ClientConfig.bloodEffects = !ClientConfig.bloodEffects;
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.blood_effects." + (ClientConfig.bloodEffects ? "on" : "off"))));
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 - 8)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.blood_effects.tooltip"))))
         .m_253136_();
      Button exportCustomArmors = Button.m_253074_(Component.m_237115_(EpicFightMod.format("gui.%s.export_custom_armor")), button -> {
            File resourcePackDirectory = Minecraft.m_91087_().m_245161_().toFile();

            try {
               HumanoidModelBaker.exportModels(resourcePackDirectory);
               Util.m_137581_().m_137644_(resourcePackDirectory);
            } catch (IOException e) {
               EpicFightMod.LOGGER.info("Failed to export custom armor models");
               e.printStackTrace();
            }
         })
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.export_custom_armor.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(bloodEffectsButton, exportCustomArmors);
      buttonHeight += 24;
      Button enablePovAction = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.enable_pov_action." + (ClientConfig.enablePovAction ? "on" : "off"))), button -> {
               ClientConfig.enablePovAction = !ClientConfig.enablePovAction;
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.enable_pov_action." + (ClientConfig.enablePovAction ? "on" : "off"))));
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.enable_pov_action.tooltip"))))
         .m_253136_();
      Button uiSetupButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.ui_setup")), button -> this.f_96541_.m_91152_(new UISetupScreen(this))
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.ui_setup.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(enablePovAction, uiSetupButton);
      buttonHeight += 24;
      Button showEpicfightAttributesButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.show_attributes." + (ClientConfig.showEpicFightAttributesInTooltip ? "on" : "off"))),
            button -> {
               ClientConfig.showEpicFightAttributesInTooltip = !ClientConfig.showEpicFightAttributesInTooltip;
               button.m_93666_(
                  Component.m_237115_(EpicFightMod.format("gui.%s.show_attributes." + (ClientConfig.showEpicFightAttributesInTooltip ? "on" : "off")))
               );
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.show_attributes.tooltip"))))
         .m_253136_();
      Button maxHitProjectilesButton = Button.m_253074_(
            Component.m_237110_(EpicFightMod.format("gui.%s.max_stuck_projectiles"), new Object[]{String.valueOf(ClientConfig.maxStuckProjectiles)}),
            button -> {
               ClientConfig.maxStuckProjectiles = (ClientConfig.maxStuckProjectiles + 1) % 30;
               button.m_93666_(
                  Component.m_237110_(EpicFightMod.format("gui.%s.max_stuck_projectiles"), new Object[]{String.valueOf(ClientConfig.maxStuckProjectiles)})
               );
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.max_stuck_projectiles.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(showEpicfightAttributesButton, maxHitProjectilesButton);
      buttonHeight += 24;
      Button enableMineBlockGuideButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.mine_block_guide." + ClientConfig.mineBlockGuideOption.m_7912_())), button -> {
               ClientConfig.mineBlockGuideOption = ClientConfig.mineBlockGuideOption.nextEnum();
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.mine_block_guide." + ClientConfig.mineBlockGuideOption.m_7912_())));
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.mine_block_guide.tooltip"))))
         .m_253136_();
      Button enableTargetEntityGuide = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.enable_target_entity_guide." + (ClientConfig.enableTargetEntityGuide ? "on" : "off"))),
            button -> {
               ClientConfig.enableTargetEntityGuide = !ClientConfig.enableTargetEntityGuide;
               button.m_93666_(
                  Component.m_237115_(EpicFightMod.format("gui.%s.enable_target_entity_guide." + (ClientConfig.enableTargetEntityGuide ? "on" : "off")))
               );
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.enable_target_entity_guide.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(enableMineBlockGuideButton, enableTargetEntityGuide);
      buttonHeight += 24;
      Button firstPersonModelButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.first_person_model." + (ClientConfig.enableAnimatedFirstPersonModel ? "on" : "off"))),
            button -> {
               ClientConfig.enableAnimatedFirstPersonModel = !ClientConfig.enableAnimatedFirstPersonModel;
               button.m_93666_(
                  Component.m_237115_(EpicFightMod.format("gui.%s.first_person_model." + (ClientConfig.enableAnimatedFirstPersonModel ? "on" : "off")))
               );
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.first_person_model.tooltip"))))
         .m_253136_();
      Button enablePlayerVanillaModelButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.enable_player_vanilla_model." + (ClientConfig.enableOriginalModel ? "on" : "off"))), button -> {
               ClientConfig.enableOriginalModel = !ClientConfig.enableOriginalModel;
               button.m_93666_(
                  Component.m_237115_(EpicFightMod.format("gui.%s.enable_player_vanilla_model." + (ClientConfig.enableOriginalModel ? "on" : "off")))
               );
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.enable_player_vanilla_model.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(firstPersonModelButton, enablePlayerVanillaModelButton);
      buttonHeight += 24;
      Button enableCosmetics = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.enable_cosmetics." + (ClientConfig.enableCosmetics ? "on" : "off"))), button -> {
               ClientConfig.enableCosmetics = !ClientConfig.enableCosmetics;
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.enable_cosmetics." + (ClientConfig.enableCosmetics ? "on" : "off"))));
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.enable_cosmetics.tooltip"))))
         .m_253136_();
      Button useComputeShaderButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.use_compute_shader." + (ClientConfig.activateComputeShader ? "on" : "off"))), button -> {
               ClientConfig.activateComputeShader = !ClientConfig.activateComputeShader;
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.use_compute_shader." + (ClientConfig.activateComputeShader ? "on" : "off"))));
            }
         )
         .m_252794_(this.f_96543_ / 2 + 5, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.use_compute_shader.tooltip"))))
         .m_253136_();
      if (!ComputeShaderProvider.supportComputeShader()) {
         useComputeShaderButton.f_93623_ = false;
         useComputeShaderButton.m_257544_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.use_compute_shader.locked.tooltip"))));
      }

      this.optionsList.addSmall(enableCosmetics, useComputeShaderButton);
      buttonHeight += 30;
      Button groundSlamsButton = Button.m_253074_(
            Component.m_237115_(EpicFightMod.format("gui.%s.ground_slams." + (ClientConfig.groundSlams ? "on" : "off"))), button -> {
               ClientConfig.groundSlams = !ClientConfig.groundSlams;
               button.m_93666_(Component.m_237115_(EpicFightMod.format("gui.%s.ground_slams." + (ClientConfig.groundSlams ? "on" : "off"))));
            }
         )
         .m_252794_(this.f_96543_ / 2 - 165, this.f_96544_ / 4 + buttonHeight)
         .m_253046_(160, 20)
         .m_257505_(Tooltip.m_257550_(Component.m_237115_(EpicFightMod.format("gui.%s.ground_slams.tooltip"))))
         .m_253136_();
      this.optionsList.addSmall(groundSlamsButton, null);
      buttonHeight += 30;
      this.optionsList
         .addBig(
            new ColorSlider(
               this.f_96547_,
               this.f_96543_ / 2 - 150,
               this.f_96544_ / 4 + buttonHeight,
               300,
               20,
               Component.m_237115_(EpicFightMod.format("gui.%s.target_outline_color")),
               ColorSlider.Style.CLASSIC,
               ClientConfig.targetOutlineColor,
               (position, color) -> ClientConfig.targetOutlineColor = position
            )
         );
      this.m_7787_(this.optionsList);
      this.maybeDisableCameraButtons(cameraTypeButton, cameraSetupButton);
   }

   private void maybeDisableCameraButtons(Button cameraTypeButton, Button cameraSetupButton) {
      EpicFightTpsCameraDisabledReason tpsDisabledReason = EpicFightTpsCameraDisableState.getReason();
      if (tpsDisabledReason != null) {
         cameraTypeButton.f_93623_ = false;
         cameraSetupButton.f_93623_ = false;
         Tooltip disabledReasonTooltip = Tooltip.m_257550_(
            Component.m_237110_("gui.epicfight.tps_perspective.disabled_due_to_mod_conflict", new Object[]{tpsDisabledReason.getModName()})
         );
         cameraTypeButton.m_257544_(disabledReasonTooltip);
         cameraSetupButton.m_257544_(disabledReasonTooltip);
      }
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      ClientEngine.getInstance().renderEngine.versionNotifier.render(guiGraphics, false);
      this.basicListRender(guiGraphics, this.optionsList, mouseX, mouseY, partialTicks);
   }
}
