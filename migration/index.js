import yargs from "yargs";
import chalk from "chalk";
import * as path from "path";
import * as fs from "fs";
import { hideBin } from "yargs/helpers";
import YAML from "yaml";
import { xml2js } from "xml-js";

const error = chalk.bold.red;
const debug = chalk.yellow;

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
const properties = {
  keyboardActionType: "type",
  movementSequence: "movement_sequence",
  inputString: "lower_case",
  inputCapsLockString: "upper_case",
  inputKey: "key_code",
};

function extractProperty(elements, prop) {
  return text;
}

function processAction(elements, yaml) {
  const data = {};
  Object.entries(properties).forEach(([prop, entry]) => {
    const find = elements.find(({ name }) => name === prop);
    if (!find) return;
    let {
      elements: [{ text }],
    } = find;

    if (!text) return;

    switch (entry) {
      case "type":
      case "key_code":
        text = text.toLowerCase();
        break;
      case "movement_sequence":
        text = text
          .toLowerCase()
          .split(";")
          .filter((s) => s !== "");
        break;
      default:
    }

    data[entry] = text;
  });

  const find = elements.find(({ name }) => name === "flags");

  if (find) {
    const flags = find.elements
      .filter(({ name }) => name === "flag")
      .reduce(
        (flags, { elements: [{ text }] }) => flags | parseInt(text, 10),
        0
      );
    if (flags) {
      data.flags = flags;
    }
  }
  switch (data.type) {
    case "input_key":
      if (!yaml.layers.hidden) {
        yaml.layers.hidden = [];
      }
      yaml.layers.hidden.push(data);
      break;
    case "input_text":
      delete data.type;
      if (data.lower_case.toUpperCase() === data.upper_case) {
        delete data.upper_case;
      }
      const quadrant = detectQuadrant(data.movement_sequence);
      if (quadrant) {
        delete data.movement_sequence;
        if (!yaml.layers.default) {
          yaml.layers.default = { sectors: {} };
          const [sector, part, position] = quadrant;
          if (!yaml.layers.default.sectors[sector]) {
            yaml.layers.default.sectors[sector] = { parts: {} };
          }
          if (!yaml.layers.default.sectors[sector].parts[part]) {
            yaml.layers.default.sectors[sector].parts[part] = [
              null,
              null,
              null,
              null,
            ];
          }
          yaml.layers.default.sectors[sector].parts[part][position] = data;
        } else {
          if (!yaml.layers.hidden) {
            yaml.layers.hidden = [];
          }
          yaml.layers.hidden.push(data);
        }
      }
      break;
  }
  console.log(debug(JSON.stringify(data)));
}
function detectQuadrant(movementSequence) {
  if (
    movementSequence[0] !== "inside_circle" ||
    movementSequence[movementSequence.length - 1] !== "inside_circle"
  )
    return null;
  movementSequence.pop();
  movementSequence.shift();
  const result = [];
  result.push(movementSequence.shift());
  result.push();
}
try {
  const yaml = { layers: {} };
  const xml = fs.readFileSync(argv.xml, "utf8");
  const result = xml2js(xml);
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
  console.log(yaml);
  //   console.log(JSON.stringify(result, null, 2));
} catch (e) {
  console.error(error(e));
  process.exit(1);
}
