const fullRotations = [
  "right;top;left;bottom;right",
  "right;bottom;left;top;right",
  "top;right;bottom;left;top",
  "top;left;bottom;right;top",
  "bottom;right;top;left;bottom",
  "bottom;left;top;right;bottom",
  "left;top;right;bottom;left",
  "left;bottom;right;top;left",
];
const properties = {
  keyboardActionType: "type",
  movementSequence: "movement_sequence",
  inputString: "lower_case",
  inputCapsLockString: "upper_case",
  inputKey: "key_code",
};
export function cleanYaml(yaml) {
  if (!yaml.layers.default || !yaml.layers.default.sectors) return;

  for (const sectorName in yaml.layers.default.sectors) {
    const sector = yaml.layers.default.sectors[sectorName];
    if (!sector.parts) {
      continue;
    }
    for (const part in sector.parts) {
      const actions = sector.parts[part];
      while (actions[actions.length - 1] === null) {
        actions.pop();
      }
    }
  }
}

export function processAction(elements, yaml) {
  const data = {};
  Object.entries(properties).forEach(([prop, entry]) => {
    const find = elements.find(({ name }) => name === prop);
    if (!find) return;
    let text = " ";
    if (find.elements) {
      text = find.elements[0].text;
    }

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
      .reduce((flags, { elements: [{ text }] }) => flags | text, 0);
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
      if (isUpperCaseInput(data, yaml)) {
        return;
      }

      delete data.type;
      delete data.flags;

      if (data.lower_case.toUpperCase() === data.upper_case) {
        delete data.upper_case;
      }
      const movementSequence = Array.from(data.movement_sequence);
      const isFullRotation = detectFullRotation(movementSequence);
      const quadrant = detectQuadrant(movementSequence);
      if (isFullRotation && quadrant) {
        return;
      } else if (quadrant) {
        delete data.movement_sequence;
        if (!yaml.layers.default) {
          yaml.layers.default = { sectors: {} };
        }
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
      break;
  }
}

function detectFullRotation(movementSequence) {
  if (
    movementSequence[0] !== "inside_circle" ||
    movementSequence[movementSequence.length - 1] !== "inside_circle"
  )
    return false;

  movementSequence.pop();
  movementSequence.shift();

  if (movementSequence.length < 6) return false;

  const movementSequenceString = movementSequence.join(";");

  if (
    !fullRotations.find((sequence) =>
      movementSequenceString.startsWith(sequence)
    )
  )
    return false;
  const start = movementSequence.shift();
  while (movementSequence[0] !== start) {
    movementSequence.shift();
  }
  return true;
}
function detectQuadrant(movementSequence) {
  if (movementSequence.length < 2) return null;

  const sector = movementSequence.shift();
  const part = movementSequence.shift();
  let last = part;
  let position = 0;

  while (movementSequence.length) {
    if (position > 3) {
      return null;
    }
    const expected = nextPosition(sector, part, last);
    const next = movementSequence.shift();
    if (expected !== next) {
      return null;
    }
    last = next;
    position++;
  }

  return [sector, part, position];
}

function nextPosition(sector, part, last) {
  const oppositeSector = opposite(sector);
  const oppositePart = opposite(part);
  if (last === oppositePart) {
    return sector;
  } else if (last === oppositeSector) {
    return oppositePart;
  } else if (last === part) {
    return oppositeSector;
  } else {
    return part;
  }
}

function opposite(part) {
  switch (part) {
    case "right":
      return "left";
    case "left":
      return "right";
    case "top":
      return "bottom";
    case "bottom":
      return "top";
  }
}

function isUpperCaseInput(data, yaml) {
  const find = ({ lower_case, upper_case }) => data.upper_case === lower_case;
  if (yaml.layers.hidden) {
    if (
      yaml.layers.hidden.filter(({ type }) => type !== "input_key").find(find)
    ) {
      return true;
    }
  }
  if (yaml.layers.default && yaml.layers.default.sectors) {
    for (const sectorName in yaml.layers.default.sectors) {
      const sector = yaml.layers.default.sectors[sectorName];
      if (!sector.parts) {
        continue;
      }
      for (const part in sector.parts) {
        if (sector.parts[part].filter((action) => !!action).find(find)) {
          return true;
        }
      }
    }
  }
  return false;
}
