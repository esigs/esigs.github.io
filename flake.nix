{
    description = "Dev tooling";

    inputs.nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.05";
    inputs.flake-utils.url = "github:numtide/flake-utils";

    outputs = { self, nixpkgs, flake-utils }:
        flake-utils.lib.eachDefaultSystem (system:
            let
                pkgs = import nixpkgs { inherit system; };
            in {
                devShells = {
                    default = import ./nix/devshell-dev.nix { inherit pkgs; };
                };
            });
}
