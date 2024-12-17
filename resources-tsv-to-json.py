import json
import sys
import csv
from collections import defaultdict

def add_gb_unit(value):
    """Add GB unit to memory values if missing."""
    if not value or value == '':
        return value
    try:
        float(value)  # Check if it's a number
        return f"{value} GB"
    except ValueError:
        return value

def clean_number(value):
    """Convert number to string without decimal places if possible."""
    if isinstance(value, (int, float)):
        return str(int(value)) if value.is_integer() else f"{value:.3f}".rstrip('0').rstrip('.')
    return value

def determine_retry_strategy(memory_value, base_memory):
    """Determine retry strategy based on the value and base memory."""
    if not memory_value or not base_memory:
        return None
        
    try:
        value = float(memory_value)
        base = float(base_memory)
        
        # Always use add strategy
        difference = value - base
        return {"strategy": "add", "operand": f"{clean_number(difference)} GB"}
    except ValueError:
        return None

def process_tsv(tsv_file):
    """Process TSV file and convert to JSON structure."""
    # Read TSV file
    with open(tsv_file, 'r') as f:
        reader = csv.reader(f, delimiter='\t')
        headers = next(reader)
        rows = list(reader)
    
    # Initialize JSON structure
    json_data = defaultdict(lambda: defaultdict(dict))
    processes = headers[2:]  # First two columns are node_type and attribute
    
    # Group rows by node type
    node_rows = defaultdict(list)
    for row in rows:
        node_type = row[0]
        node_rows[node_type].append(row)
    
    # Process each node type
    for node_type, node_data in node_rows.items():
        # Create dictionaries to store attributes
        attributes = defaultdict(dict)
        for row in node_data:
            attr = row[1]
            values = row[2:]
            
            # Store values for each process
            for process_name, value in zip(processes, values):
                if attr.startswith('cpu_'):
                    sub_attr = attr[4:]  # Remove 'cpu_' prefix
                    if 'cpus' not in attributes[process_name]:
                        attributes[process_name]['cpus'] = {}
                    if value != '':
                        try:
                            float_val = float(value)
                            # Convert to int if it's a whole number
                            if float_val.is_integer():
                                attributes[process_name]['cpus'][sub_attr] = int(float_val)
                            else:
                                # Use clean_number for consistent decimal places
                                attributes[process_name]['cpus'][sub_attr] = float(clean_number(float_val))
                        except ValueError:
                            attributes[process_name]['cpus'][sub_attr] = value
                
                elif attr.startswith('mem_'):
                    sub_attr = attr[4:]  # Remove 'mem_' prefix
                    if 'memory' not in attributes[process_name]:
                        attributes[process_name]['memory'] = {}
                    if value != '':
                        if sub_attr == 'fraction':
                            try:
                                float_val = float(value)
                                if float_val.is_integer():
                                    attributes[process_name]['memory'][sub_attr] = int(float_val)
                                else:
                                    attributes[process_name]['memory'][sub_attr] = float(clean_number(float_val))
                            except ValueError:
                                attributes[process_name]['memory'][sub_attr] = value
                        else:
                            attributes[process_name]['memory'][sub_attr] = add_gb_unit(value)
                
                elif attr == 'retry_strategy' and value:
                    if process_name in attributes and 'memory' in attributes[process_name]:
                        base_memory = attributes[process_name]['memory'].get('max', '0')
                        if base_memory != '':
                            retry = determine_retry_strategy(value, base_memory.split()[0])
                            if retry:
                                attributes[process_name]['retry_strategy'] = {'memory': retry}
        
        # Add to JSON structure
        for process_name, process_data in attributes.items():
            if process_data:  # Only add if there's actual data
                # Remove empty dictionaries
                if 'cpus' in process_data and not process_data['cpus']:
                    del process_data['cpus']
                if 'memory' in process_data and not process_data['memory']:
                    del process_data['memory']
                    
                # Only add process if it has some configuration
                if process_data.get('cpus') or process_data.get('memory'):
                    json_data[node_type][process_name] = process_data
    
    return dict(json_data)  # Convert defaultdict to regular dict

def write_json(data, output_file=None):
    """Write JSON data to file or stdout."""
    if output_file:
        with open(output_file, 'w') as f:
            json.dump(data, f, indent=4)
    else:
        print(json.dumps(data, indent=4))

def main():
    if len(sys.argv) < 2:
        print("Usage: python script.py input.tsv [output.json]")
        sys.exit(1)
        
    # Process TSV and convert to JSON
    json_data = process_tsv(sys.argv[1])
    
    # Write output
    output_file = sys.argv[2] if len(sys.argv) > 2 else None
    write_json(json_data, output_file)

if __name__ == '__main__':
    main()