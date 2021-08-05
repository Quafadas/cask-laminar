package example.viz

object Pie {
  val pieSpec: String = raw"""
  {
  "$$schema": "https://vega.github.io/schema/vega/v5.json",
  "description": "A basic pie chart example.",
  "signals" : [
      {
            "name": "width",
            "init": "isFinite(containerSize()[0]) ? containerSize()[0] : 200",
            "on": [
              {
                "update": "isFinite(containerSize()[0]) ? containerSize()[0] : 200",
                "events": "window:resize"
              }
            ]
          },
          {
            "name": "height",
            "init": "isFinite(containerSize()[1]) ? containerSize()[1] : 200",
            "on": [
              {
                "update": "isFinite(containerSize()[1]) ? containerSize()[1] : 200",
                "events": "window:resize"
              }
            ]
          }, {
            "name": "tooltip",
            "value": {},
            "on": [
              {"events": "rect:mouseover", "update": "datum"},
              {"events": "rect:mouseout",  "update": "{}"}
            ]
          },
          {
            "name": "active",
            "value": {},
            "on": [
              {"events": "@wordMarks:mousedown, @wordMarks:touchstart", "update": "pluck(datum, 'text')"},
              {"events": "window:mouseup, window:touchend", "update": "{}"}
            ]
          },
    {"name" : "widthover2", "update": "width / 2"   },
    {"name" : "widthover10", "update": "width/10"   }
    

  ],

  "data": [
    {
      "name": "table",
      "values": [
        {"id": "open", "field": 4},
        {"id": "closed", "field": 6}
      ],
      "transform": [
        {
          "type": "pie",
          "field": "field"     
        }
      ]
    }
  ],

  "scales": [
    {
      "name": "color",
      "type": "ordinal",
      "domain": {"data": "table", "field": "id"},
      "range": {"scheme": "category20"}
    },
    {
      "name": "r",      
      "type": "sqrt",
      "domain": {"data": "table", "field": "field"},
      "zero": true,
      "range": [0, 0]
    }
  ],

  "marks": [
    {
      "type": "arc",
      "from": {"data": "table"},
      "encode": {
        "enter": {
          "fill": {"scale": "color", "field": "id"},
          "x": {"signal": "width / 2"},
          "y": {"signal": "height / 2"}
        },
        "update": {
          "startAngle": {"field": "startAngle"},
          "endAngle": {"field": "endAngle"},
          "padAngle": [{"value": 0}],
          "innerRadius": {"signal": "width / 2.5"},
          "outerRadius": {"signal": "width / 2"},
          "cornerRadius": [{"value": 0}]
        }
      }
    },
       {
      "type": "text",
      "from": {"data": "table"},
      "encode": {
        "enter": {
          "x": {"field": {"group": "width"}, "mult": 0.5},
          "y": {"field": {"group": "height"}, "mult": 0.5},
          "radius": {"scale": "r", "field": "field", "offset": {"signal": "width / 4"}},
          "theta": {"signal": "(datum.startAngle + datum.endAngle)/2"},          
          "text": {"field": "id"}
        }
      }
    }
  ]
}
"""
}
