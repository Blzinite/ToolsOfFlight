# Tools of Flight
A mod that adds a data component that allows the player to fly by riding an item with the component.
Works pretty much like a elytra for gate-keeping the end but not gate-keeping flight as a travel method.

## Data-Driven
The following command gives the player a iron starts with a 10 tick boost and using a blaze powder will give it another 10 ticks of boost
```
/give @s minecraft:iron_sword[flight_tools:boost_holder={inital_boost:10,boost_items:[{item:"blaze_powder",boost:10}]}]
```

The follow recipe json is an example on how to add a recipe that allows player to craft flying tools
```
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    " . ",
    ".#.",
    " . "
  ],
  "key": {
    "#": {
      "item": "minecraft:iron_sword"
    },
    ".": {
      "item": "minecraft:blaze_powder"
    }
  },
  "result": {
    "id": "minecraft:iron_sword",
    "components": {
      "flight_tools:boost_holder": {
        "inital_boost": 10,
        "boost_items": [
          {
            "item": "blaze_powder",
            "boost": 10
          }
        ]
      }
    }
  }
}
```