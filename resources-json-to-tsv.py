import json
import sys
from collections import defaultdict

def clean_number(value):
    """Convert number to string without decimal places if possible."""
    if not value and value != 0:
        return value
    if isinstance(value, (int, float)):
        if isinstance(value, int):
            return str(value)
        return str(int(value)) if value.is_integer() else f"{value:.3f}".rstrip('0').rstrip('.')
    return value

def convert_to_gb(memory_str):
    """Convert memory string to GB value without unit."""
    if not memory_str:
        return ''
    if isinstance(memory_str, (int, float)):
        return clean_number(float(memory_str))
    
    value = float(memory_str.split()[0])
    unit = memory_str.split()[1].upper()
    
    if unit == 'TB':
        return clean_number(value * 1024)
    elif unit == 'GB':
        return clean_number(value)
    elif unit == 'MB':
        return clean_number(value / 1024)
    elif unit == 'KB':
        return clean_number(value / 1024 / 1024)
    return clean_number(value)

def extract_resource_info(config):
    """Extract CPU and memory information from a process configuration."""
    info = {
        'cpu_min': clean_number(config.get('cpus', {}).get('min', '')),
        'cpu_fraction': clean_number(config.get('cpus', {}).get('fraction', '')),
        'cpu_max': clean_number(config.get('cpus', {}).get('max', '')),
        'mem_min': convert_to_gb(config.get('memory', {}).get('min', '')),
        'mem_fraction': config.get('memory', {}).get('fraction', ''),
        'mem_max': convert_to_gb(config.get('memory', {}).get('max', '')),
    }
    
    # Add retry strategy if present
    if 'retry_strategy' in config and 'memory' in config['retry_strategy']:
        retry = config['retry_strategy']['memory']
        strategy = retry['strategy']
        operand = float(convert_to_gb(retry['operand']))
        base_memory = float(convert_to_gb(config.get('memory', {}).get('max', '0')))
        
        # For all strategies, calculate the final value that would be used
        if strategy == 'exponential':
            retry_value = base_memory * operand
        else:  # treat everything as 'add'
            retry_value = base_memory + operand
            
        info['retry_strategy'] = clean_number(retry_value)
    else:
        info['retry_strategy'] = ''
        
    return info

def process_json(json_data):
    """Process the JSON data and organize it by node type."""
    # Use f72 section to determine process order
    # Get processes in f72 order
    f72_processes = list(json_data['f72'].keys())
    
    # Get any remaining processes not in f72
    all_processes = set()
    for node_type in json_data.values():
        all_processes.update(node_type.keys())
    remaining_processes = sorted(list(all_processes - set(f72_processes)))
    
    # Combine ordered f72 processes with any remaining processes
    processes = f72_processes + remaining_processes
    
    # Get and sort node types
    def node_sort_key(node_type):
        if node_type == 'default':
            return float('inf')  # Makes 'default' appear last
        elif node_type == 'm64':
            return float('inf') - 1  # Makes 'm64' appear just before 'default'
        else:
            # Extract number from node type (e.g., 'f2' -> 2)
            return int(node_type[1:])
    
    node_types = sorted(json_data.keys(), key=node_sort_key)
    
    # Define resource attributes in desired order
    attributes = [
        'cpu_min', 'cpu_fraction', 'cpu_max',  # CPU attributes first
        'mem_min', 'mem_fraction', 'mem_max',  # Memory attributes second
        'retry_strategy'  # Retry strategy last
    ]
    
    # Create header - processes become columns
    headers = ['node_type', 'attribute']
    headers.extend(processes)
    
    # Create rows - grouped by attribute type first
    rows = []
    # First all CPU attributes for all nodes
    for attr in ['cpu_min', 'cpu_fraction', 'cpu_max']:
        for node in node_types:
            row = [node, attr]
            for process in processes:
                if process in json_data[node]:
                    info = extract_resource_info(json_data[node][process])
                    value = info[attr]
                    # Clean any numeric values
                    try:
                        if value != '':
                            value = clean_number(float(value))
                    except ValueError:
                        pass
                    row.append(value)
                else:
                    row.append('')
            rows.append(row)
    
    # Then all memory attributes for all nodes
    for attr in ['mem_min', 'mem_fraction', 'mem_max']:
        for node in node_types:
            row = [node, attr]
            for process in processes:
                if process in json_data[node]:
                    info = extract_resource_info(json_data[node][process])
                    row.append(info[attr])
                else:
                    row.append('')
            rows.append(row)
    
    # Finally retry strategy for all nodes
    for node in node_types:
        row = [node, 'retry_strategy']
        for process in processes:
            if process in json_data[node]:
                info = extract_resource_info(json_data[node][process])
                row.append(info['retry_strategy'])
            else:
                row.append('')
        rows.append(row)
    
    return headers, rows

def write_tsv(headers, rows, output_file=None):
    """Write the data to TSV format."""
    if output_file:
        f = open(output_file, 'w')
    else:
        f = sys.stdout
        
    try:
        # Write header
        f.write('\t'.join(str(x) for x in headers) + '\n')
        
        # Write rows
        for row in rows:
            f.write('\t'.join(str(x) for x in row) + '\n')
    
    finally:
        if output_file:
            f.close()

def main():
    if len(sys.argv) < 2:
        print("Usage: python script.py input.json [output.tsv]")
        sys.exit(1)
        
    # Read input JSON
    with open(sys.argv[1], 'r') as f:
        json_data = json.load(f)
    
    # Process the data
    headers, rows = process_json(json_data)
    
    # Write output
    output_file = sys.argv[2] if len(sys.argv) > 2 else None
    write_tsv(headers, rows, output_file)

if __name__ == '__main__':
    main()