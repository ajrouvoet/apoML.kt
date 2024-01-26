{ pkgs, stdenv, gradle, jdk17, jdk21, ... }:

stdenv.mkDerivation rec {
  pname   = "intensely-functional";
  version = "latest";
  name    = "${pname}-${version}";

  src = ./.;
  buildInputs = [ gradle jdk17 ];

  JDK21 = jdk21;
  NIX_SHELL_NAME = "Intensely Functional";
}
