#!/bin/bash
curl -XPOST 'http://10.102.107.7:9200/newspicks' -d @mapping_newspicks.json
