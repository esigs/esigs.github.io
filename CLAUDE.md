# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Clojure Polylith workspace with the following namespace structure:
- **Top namespace**: `ca.ericsigurdson`
- **Interface namespace**: `interface`

Polylith is an architecture that enables building composable systems using reusable components and bases, organized in a monorepo structure.

## Development Environment

This project uses Nix flakes for environment management. The development shell is defined in `nix/devshell-dev.nix` and includes:
- JDK 21
- Clojure CLI tools
- Polylith CLI
- GitHub CLI (gh)
- Node.js (for Claude Code)

### Setting up the environment

```bash
# Enter the Nix development shell
nix develop

# Or use direnv if configured
direnv allow
```

The shell automatically sets `APP_ENV=DEV` and provides a `claude` alias for `npx @anthropic-ai/claude-code`.

## Common Commands

### REPL and Development
```bash
# Start a Clojure REPL
clj -M:repl

# Start REPL with development profile
clj -M:dev
```

### Polylith Commands
```bash
# Show workspace information and structure
poly info

# Run all tests
poly test

# Check workspace for circular dependencies and issues
poly check

# Create a new component
poly create component NAME

# Create a new base
poly create base NAME

# Create a new project
poly create project NAME
```

### Testing
```bash
# Run all tests
poly test

# Run tests for a specific component
clj -M:test -n ca.ericsigurdson.COMPONENT-NAME.interface-test
```

## Architecture

### Polylith Workspace Structure

```
workspace.edn           # Workspace configuration
deps.edn               # Dependencies and aliases
bases/                 # Base implementations (entry points)
components/            # Reusable components (business logic)
projects/              # Deployable projects
development/           # Development-time code and utilities
```

### Key Concepts

- **Components**: Reusable building blocks containing business logic. Each component exposes an interface namespace (e.g., `ca.ericsigurdson.COMPONENT.interface`) that other components and bases use.

- **Bases**: Entry points for the system (e.g., web servers, CLIs). Bases consume components but cannot be consumed by other parts of the workspace.

- **Projects**: Artifacts that combine bases and components into deployable applications.

- **Development project**: A special project (aliased as `:dev`) that includes all code for development purposes. This allows you to work with all components simultaneously in the REPL.

### Namespace Convention

All code follows the pattern: `ca.ericsigurdson.<brick-name>.interface` for public APIs, where brick-name is either a component or base name.

## Version Control

The workspace is configured with:
- Git VCS
- Auto-add disabled (`vcs {:auto-add false}`)
- Tag patterns: `stable-*` for stable releases, `v[0-9]*` for version releases

## Dependencies

Project dependencies are managed in `deps.edn`:
- Clojure 1.11.1
- Polylith CLI tools 0.2.21
