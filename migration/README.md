# Layout file migration tool

This tool yelps to migrate the old layout format (`XML`) to the new one (`YAML`).

The migration is following the best effort strategy.
All characters with movements matching the default layer will be placed under
the default layers. All other cases will be moved under the `hidden` layer.

You might need to manually update the file in order to render extra layers.

## Usage

The tool requires node to be install.

```sh
cd migration
npm i -g .
8vim-migration -h

```
