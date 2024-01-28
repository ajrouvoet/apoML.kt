{ site, apoml-src }:
{ config, lib, pkgs, ... }: {
    users.users = {
        # of course apoML is developed by Apo
        apo = {
            isNormalUser = true; # well...
            extraGroups  = [ "wheel" ];
            password     = "greekyoghurt";
        };
    };

    virtualisation.vmVariant = {
        virtualisation = {
            memorySize = 8192;
            cores      = 4;
        };
    };

    environment.systemPackages = with pkgs; [
        git
        ripgrep
        jetbrains.idea-community
        jdk17
        gradle
    ];

    environment.variables = {
        JDK17 = pkgs.jdk17;
        src   = apoml-src;
    };

    services.xserver = {
        enable = true;
        desktopManager.plasma5.enable = true;
        displayManager.defaultSession = "plasmawayland";
        displayManager.sddm = {
          enable = true;
        };
    };

    networking.extraHosts = ''
        127.0.0.1 apo.ml
    '';

    services.nginx = {
        enable = true;
        virtualHosts = {
            "apo.ml" = {
                locations."/" = {
                    alias = "${site}/www/";
                };
            };
        };
    };

    environment.plasma5.excludePackages = with pkgs.libsForQt5; [
      oxygen
      khelpcenter
      plasma-browser-integration
      print-manager
    ];

    system.stateVersion = "23.11";
}
