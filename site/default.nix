{ pkgs, lib, stdenv, python3, ... }:

stdenv.mkDerivation rec {
    pname   = "apoML-site";
    version = "latest";
    name    = "${pname}-${version}";

    src = ./.;
    buildInputs = [
        (python3.withPackages (pypi: with pypi; [
            mkdocs-material
        ]))
    ];

    buildPhase = ''
        mkdocs build
    '';

    installPhase = ''
        mkdir -p $out/www/
        cp site/* $out/www/
    '';
}