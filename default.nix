{ pkgs, stdenv, gradle, jdk17, ... }:

stdenv.mkDerivation rec {
  pname   = "intensely-functional";
  version = "latest";
  name    = "${pname}-${version}";

  src = ./.;
  buildInputs = [ gradle jdk17 ];

  NIX_SHELL_NAME = "Intensely Functional";
}
