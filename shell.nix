{ pkgs ? import <nixpkgs> {} }:

let
  libs = with pkgs; [
    libpulseaudio
    libGL
    glfw
    openal
    stdenv.cc.cc.lib
  ];
in
pkgs.mkShell {
  packages = [];
  buildInputs = libs;
  LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath libs;
}

