{
  description = "Moneytor project";

  outputs = { self, nixpkgs }: 
  let
        system = "x86_64-linux";
	pkgs = import nixpkgs { inherit system; };
  in {

    packages.x86_64-linux.moneytor = import ./default.nix pkgs;
    packages.x86_64-linux.webapp   = import ./webapp/default.nix pkgs;

    packages.x86_64-linux.default = self.packages.x86_64-linux.moneytor;
  };
}
