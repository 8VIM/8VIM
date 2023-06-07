#!/usr/bin/env node
import yargs from "yargs";
import chalk from "chalk";
import * as path from "path";
import * as fs from "fs/promises";
import { hideBin } from "yargs/helpers";
import YAML from "yaml";
import { xml2js } from "xml-js";
import { cleanYaml, processAction } from "./functions.js";
import { confirm } from "@inquirer/prompts";

const argv = yargs(hideBin(process.argv))
  .command("$0 <xml> [output]", "Migrate XML to YAML", (yargs) =>
    yargs
      .option("force", {
        alias: "f",
        describe: "Force overwrite of the generated file",
        boolean: true,
      })
      .positional("xml", {
        describe: "Path of the XML to convert",
        normalize: true,
      })
      .positional("output", {
        describe: "Directory path where the yaml file willl be generated",
        default: ".",
        normalize: true,
      })
      .demandOption("xml")
  )
  .help()
  .version(false).argv;

const invalidXML = "Invalid XML layout file";
const output = path.resolve(argv.output, `${path.parse(argv.xml).name}.yaml`);

async function checkOutput() {
  try {
    const stat = await fs.stat(output);
    if (!argv.force && stat.isFile()) {
      const answer = await confirm({
        message: `${output} already exists. Do you want to overwrite it`,
      });
      if (!answer) process.exit(0);
    }
  } catch (error) {}
}

try {
  await checkOutput();
  console.log(chalk.yellow(`Migrating ${argv.xml} to ${output}`));
  const yaml = { layers: {} };
  const xml = await fs.readFile(argv.xml, "utf8");
  const result = xml2js(xml, { ignoreComment: true, nativeType: true });
  if (
    !result.elements.length ||
    result.elements[0].type !== "element" ||
    result.elements[0].name !== "keyboardData"
  )
    throw new Error(invalidXML);

  const actionMap = result.elements[0].elements.find(
    ({ name }) => name === "keyboardActionMap"
  );

  if (!actionMap) throw new Error(invalidXML);
  actionMap.elements
    .filter(({ name, type }) => name === "keyboardAction" && type === "element")
    .forEach(({ elements }) => {
      if (elements) processAction(elements, yaml);
    });
  cleanYaml(yaml);
  await fs.writeFile(output, YAML.stringify(yaml));
  console.log(chalk.green("Migration successful"));
} catch (e) {
  console.error(chalk.bold.red(e));
  console.error(chalk.bold.red(e.stack));
  process.exit(1);
}
