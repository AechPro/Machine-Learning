def parse_encoding_string(table):
    encodingTable = []
    try:
        lines = table.split("_")
        for line in lines:
            entry = []
            args = line.replace("_", " ").split(" ")
            for i in range(len(args)):
                try:
                    args[i].strip()
                    values = []
                    if ',' in args[i]:
                        parameterRange = args[i].split(",")
                        for arg in parameterRange:
                            if len(arg) > 1 and '.' in arg:
                                values.append(float(arg))
                            else:
                                values.append(int(arg))
                    else:
                        arg = args[i]
                        if len(arg) > 1 and '.' in arg:
                            values.append(float(arg))
                        else:
                            values.append(int(arg))
                    entry.append(values)
                except:
                    entry.append([args[i].strip()])
            encodingTable.append(entry)
    except Exception as e:
        print("!!!UNABLE TO READ ENCODING TABLE!!!\n\n----EXCEPTION INFORMATION----\nException type:",
              type(e).__name__, "\nException args:", e.args)
    return encodingTable