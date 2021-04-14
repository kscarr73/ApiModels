curl --request DELETE \
  --url http://localhost:9200/apimodels \
  --header 'Content-Type: application/json'

curl --request PUT \
  --url http://localhost:9200/apimodels \
  --header 'Content-Type: application/json' \
  --data '{
	"settings": {
		"number_of_shards": 1,
		"number_of_replicas": 1
	},
	"mappings": {
		"properties": {
			"className": {
				"type": "keyword"
			},
			"classType": {
				"type": "text",
				"fields": {
					"keyword": {
						"type": "keyword",
						"ignore_above": 256
					}
				}
			},
			"desc": {
				"type": "text"
			},
			"fields": {
				"properties": {
					"DisplayName": {
						"type": "text",
						"fields": {
							"keyword": {
								"type": "keyword",
								"ignore_above": 256
							}
						}
					},
					"attribute": {
						"type": "boolean"
					},
					"defaultValue": {
						"type": "text",
						"fields": {
							"keyword": {
								"type": "keyword",
								"ignore_above": 256
							}
						}
					},
					"desc": {
						"type": "text"
					},
					"format": {
						"type": "text",
						"fields": {
							"keyword": {
								"type": "keyword",
								"ignore_above": 256
							}
						}
					},
					"length": {
						"type": "long"
					},
					"max": {
						"type": "text"
					},
					"min": {
						"type": "text"
					},
					"name": {
						"type": "text"
					},
					"sampleData": {
						"type": "text",
						"fields": {
							"keyword": {
								"type": "keyword",
								"ignore_above": 256
							}
						}
					},
					"subType": {
						"type": "text"
					},
					"type": {
						"type": "text"
					}
				}
			},
			"lastUpdated": {
				"type": "date"
			},
			"name": {
				"type": "keyword"
			}
		}
	}
}'