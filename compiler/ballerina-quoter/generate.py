import json
import os


# syntax_tree_descriptor of treegen
SYNTAX_TREE_DESCRIPTOR = '../ballerina-treegen/src/main/resources/syntax_tree_descriptor.json'
PARAMETER_NAMES = 'src/main/resources/parameter-names.json'
PARAMETER_NAMES_TEST = 'src/test/resources/parameter-names.json'

dirname = os.path.dirname(__file__)

def main():
    """
    Main entry point
    """

    # Read the source json
    with open(os.path.join(dirname, SYNTAX_TREE_DESCRIPTOR), 'r') as fr:
        data = json.load(fr)

    # Process each node entry
    output = {}
    for node in data["nodes"]:
        name = node["name"]
        attributes = node.get("attributes", [])
        attributes = map(lambda attr: attr["name"], attributes)
        output[name] = list(attributes)

    # Output the json
    for filename in [PARAMETER_NAMES, PARAMETER_NAMES_TEST]:
        with open(os.path.join(dirname, filename), 'w') as fw:
            data = json.dump(output, fw)


if __name__ == "__main__":
    main()
