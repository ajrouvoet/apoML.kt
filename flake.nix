{
  description = "Functional Programming Intensive Course";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/release-23.11";
  };

  outputs = { self, nixpkgs }: 
  let
    system = "x86_64-linux";
	pkgs = import nixpkgs { inherit system; };
  in rec {

    # for the development shell for the course
    packages.x86_64-linux.fpi     = import ./default.nix pkgs;
    packages.x86_64-linux.default = self.packages.x86_64-linux.fpi;

    # for the website
    site = import ./site/default.nix pkgs;

    # for the VM
    nixosConfigurations.vm = nixpkgs.lib.nixosSystem {
        inherit system;
        modules = [
            (import ./vm.nix { inherit site; })
        ];
    };
  };
}
