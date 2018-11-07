function generateStringFromSeed(seedVal) {
// constants
    var flagSep = "_";
// variables
    var times = 1;
    var origSeed = seedVal;
    var exp = undefined;
    var debug = undefined;
    var output = "xml";
    var flagIdx = origSeed.indexOf(flagSep);
    var seedWithoutFlags = 1 * (flagIdx >= 0 ? origSeed.substring(flagIdx + 1) : origSeed);
    var old = seedWithoutFlags < 0;
    var seed = isNaN(seedWithoutFlags) ? new Date().getTime() : Math.abs(seedWithoutFlags);
    var hide = undefined;
    var c = undefined;
    var id = undefined;

    function containsCode(x) {
        return origSeed.indexOf(x) >= 0;
    }

    var user = {
        w: containsCode("w"),
        u: containsCode("u"),
        b: containsCode("b"),
        r: containsCode("r"),
        g: containsCode("g"),
        d: containsCode("d"),
        i: containsCode("i"),
        s: containsCode("s"),
        a: containsCode("a"),
        e: containsCode("e"),
        p: containsCode("p"),
        l: containsCode("l"),
        c: containsCode("c")
    };
    var userColor = user.w || user.u || user.b || user.r || user.g || user.d;
    var userType = user.i || user.s || user.a || user.e || user.p || user.l || user.c;
    var replacements =
        output === "xml" ? replacementsXml()
            : output === "mse" ? replacementsMse()
            : replacementsHtml();

    // functions
    function userFlags() {
        var f = "";
        for (var key in user)
            if (user[key])
                f += key;
        return f === "" ? "" : f + flagSep;
    }

    function rnd() {
        var x = Math.sin(seed++) * 10000;
        return x - Math.floor(x);
    }

    function mseCol(x) {
        return x === "w" ? "white" :
            x === "u" ? "blue" :
                x === "r" ? "red" :
                    x === "g" ? "green" :
                        x === "b" ? "blue" :
                            x === "c" ? "artifact" : "";
    }

    function identity() {
        id = {};
        var count = 0;
        if (user.w || (!userColor & rnd() >= 0.7)) {
            id.w = "<W />";
            count++;
        }
        if (user.u || (!userColor & rnd() >= 0.7)) {
            id.u = "<U />";
            count++;
        }
        if (user.b || (!userColor & rnd() >= 0.7)) {
            id.b = "<B />";
            count++;
        }
        if (user.r || (!userColor & rnd() >= 0.7)) {
            id.r = "<R />";
            count++;
        }
        if (user.g || (!userColor & rnd() >= 0.7)) {
            id.g = "<G />";
            count++;
        }
        var colId = "c";
        var msecolor = "";
        var msecost = "";
        for (var color in id) {
            colId = color;
            msecolor += (msecolor === "" ? "" : ", ") + mseCol(color);
            msecost += color.toUpperCase();
        }
        id.colors = count;
        id.col = count > 1 ? "m" : colId;
        id.cmc = Math.max(Math.floor(rnd() * 6), count);
        var type = rndNum(0, 9);
        if (user.i || (!userType && type === 0)) id.i = "Instant";
        if (user.s || (!userType && type === 1)) id.s = "Sorcery";
        if (user.a || (!userType && (type === 2 || type === 3))) {
            id.a = "Artifact";
            id.col = "a";
        }
        if (user.e || (!userType && (type === 4 || type === 5))) id.e = "Enchantment";
        if (user.p || (!userType && type === 6)) id.p = "Planeswalker";
        if (user.l || (!userType && type === 7)) {
            id.l = "Land";
            id.cmc = 1;
            id.col = id.col === "a" ? "c" : id.col;
        }
        if (user.c || (!userType && (type === 8 || type === 9))) id.c = "Creature";
        id.pow = rndNum(0, 9);
        id.tou = rndNum(1, 9);
        id.numColorless = id.cmc - id.colors;
        id.msecost = (id.numColorless > 0 ? id.numColorless : "") + msecost;
        id.msecolor = msecolor;
    }

    function always(x) {
        return function () {
            return x;
        }
    }

    function replacementsHtml() {
        return [
            ["<Cards>", always("")],
            ["</Cards>", always("")],
            ["<R />", always("<img class='mana' src='mana-r.png' />")],
            ["<G />", always("<img class='mana' src='mana-g.png' />")],
            ["<W />", always("<img class='mana' src='mana-w.png' />")],
            ["<U />", always("<img class='mana' src='mana-u.png' />")],
            ["<B />", always("<img class='mana' src='mana-b.png' />")],
            ["<LoyalityActions>", always("<table class='section loyality-section'>")],
            ["<LoyalityAction>", always("<tr class='loyalty-action'>")],
            ["<LoyalityCost>", always("<td class='loyalty-cost'>")],
            ["</LoyalityCost>", always("</td><td class='loyalty-colon'>:</td><td>")],
            ["<Plus />", always("<img src='loyaltyup.png' />+")],
            ["<Minus />", always("<img src='loyaltydown.png' />&ndash;")],
            ["<Zero />", always("<img src='loyaltynaught.png' />&nbsp;&nbsp;")],
            ["</LoyalityAction>", always("</td></tr>")],
            ["</LoyalityActions>", always("</table>")],
            ["<Levels>", always("<table class='section level-section'>")],
            ["<LevelUp>", always("<td class='level-part' colspan='2'>")],
            ["</LevelUp>", always("")],
            ["<Level>", always("<tr>")],
            ["<From>", always("<td class='level-part'><img class='level-from-to' src='lvlup_texture_1.png' />")],
            ["</From>", always("")],
            ["<ToAny />", always("+</td><td>")],
            ["<To>", always("-")],
            ["</To>", always(":</td><td>")],
            ["<LevelPower>", function () {
                return "<td class='level-stat stat'><img class='level-stat-box' src='pt-" + id.col + ".png' />";
            }],
            ["</LevelPower>", always("")],
            ["<LevelToughness>", always("/")],
            ["</LevelToughness>", always("</td>")],
            ["</Level>", always("</td></tr>")],
            ["</Levels>", always("</table>")],
            ["<Tap />", always("<img class='mana' src='tap.png' />")],
            ["<Mana>", always("<img class='mana' src='mana-")],
            ["</Mana>", always(".png' />")],
            ["<Card>", function () {
                return "<div class='" + (id.p ? "pw" : "") + "card'>";
            }],
            ["</Card>", always("</div></div>")],
            ["<Identity>", function () {
                return "<img class='bgcardimage' src='" + (id.p ? "pw" : "") + "card-";
            }],
            ["</Identity>", always(".jpg' /><div class='content'>")],
            ["<Name>", always("<table class='title'><tr><td class='name'>")],
            ["</Name>", always("</td>")],
            ["<Seed>", function () {
                return "<div" + (id.p || (id.c && !id.level) ? " class='seed-box'" : "") + "><a class='seed-link' href='?seed=";
            }],
            ["</Seed>", function () {
                return "'>tinyurl.com/mtggen?seed=" + id.flaggedSeed + "</a></div>";
            }],
            ["<Cost>", always("<td class='cost'>")],
            ["</Cost>", always("</td></tr></table>")],
            ["<Image>", always("<div><img class='image' src='http://magiccards.info/crop/en/")],
            ["</Image>", always(".jpg' /></div>")],
            ["<Type>", always("<div class='type'>")],
            ["</Type>", always("")],
            ["<SubType>", always(" &ndash; ")],
            ["</SubType>", always("")],
            ["<Text>", always("</div><table><tr><td class='text'>")],
            ["</Text>", always("</td></tr></table>")],
            ["<Loyalty>", always("<img class='loyalty-box' src='loyalty.png' /><div class='box-text loyalty'>")],
            ["</Loyalty>", always("</div>")],
            ["<Power>", function () {
                return id.level ? "<!--" : "<img class='box' src='pt-" + id.col + ".png' /><div class='box-text stat'>&nbsp;";
            }],
            ["</Power>", always("")],
            ["<Toughness>", always("/")],
            ["</Toughness>", function () {
                return id.level ? "-->" : "</div>";
            }],
            ["<This />", function () {
                return id.name;
            }],
            ["<NewLine />", always("<div class='newline'>&nbsp;</div>")]
        ];
    }

    function replacementsXml() {
        return [];
    }

    function replacementsMse() {
        return [
            ["<R />", always("<sym-auto>R</sym-auto>")],
            ["<G />", always("<sym-auto>G</sym-auto>")],
            ["<W />", always("<sym-auto>W</sym-auto>")],
            ["<U />", always("<sym-auto>U</sym-auto>")],
            ["<B />", always("<sym-auto>B</sym-auto>")],
            ["<LevelUp>", always("")],
            ["</LevelUp>", always("")],
            ["<LoyalityActions>", always("Â§\t")],
            ["<LoyalityAction>", always("Â§\t")],
            ["<LoyalityCost>", always("Â§\t")],
            ["</LoyalityCost>", always(":")],
            ["<Plus />", always("+")],
            ["<Minus />", always("-")],
            ["<Zero />", always("")],
            ["</LoyalityAction>", always("")],
            ["</LoyalityActions>", always("")],
            ["<Levels>", always("")],
            ["<Level>", always("")],
            ["<From>", always("")],
            ["</From>", always("")],
            ["<ToAny />", always("+")],
            ["<To>", always("-")],
            ["</To>", always(":")],
            ["</Level>", always("")],
            ["</Levels>", always("")],
            ["<Tap />", always("<sym-auto>T</sym-auto>")],
            ["<Mana>", always("<sym-auto>")],
            ["</Mana>", always("</sym-auto>")],
            ["<Identity>", function () {
                return "Â§card_color: " + id.msecolor + "Â§note: ";
            }],
            ["</Identity>", always("")],
            ["<Name>", always("Â§name: ")],
            ["</Name>", always("")],
            ["<Seed>", always("Â§note: ")],
            ["</Seed>", ""],
            ["<Cost>", function () {
                return "Â§casting_cost: " + id.msecost + "Â§note: ";
            }],
            ["</Cost>", always("")],
            ["<Image>", function () {
                return "Â§image: image" + id.counter + "Â§note: ";
            }],
            ["</Image>", always("")],
            ["<Type>", always("Â§super_type: ")],
            ["</Type>", always("")],
            ["<SubType>", always(" - ")],
            ["</SubType>", always("")],
            ["<Text>", always("Â§rule_text:Â§\t")],
            ["</Text>", always("")],
            ["<Loyalty>", always("Â§loyalty: ")],
            ["</Loyalty>", always("")],
            ["<Power>", always("Â§power: ")],
            ["</Power>", always("")],
            ["<Toughness>", always("Â§toughness: ")],
            ["</Toughness>", always("")],
            ["<This />", function () {
                return "<atom-cardname>" + id.name + "</atom-cardname>";
            }],
            ["<NewLine />", always("Â§\t")],
            ["<Card>", always("\ncard:Â§has_styling: falseÂ§type_symbol: noneÂ§time_created: 2016-01-01 00:00:00Â§time_modified: 2016-01-01 00:00:00Â§indicator: colorless")],
            ["</Card>", always("")],
            ["<", always("&lt;")],
            [">", always("&gt;")],
            ["&lt;Cards&gt;", always("<pre>mse_version: 0.3.8\ngame: magic\nstylesheet: m15\nset_info:Â§symbol: symbol1.mse-symbolÂ§automatic_reminder_text: Â§automatic_card_numbers: no\nstyling:Â§magic-m15: text_box_mana_symbols: magic-mana-small.mse-symbol-fontÂ§overlay: ")],
            ["&lt;/Cards&gt;", always("\nversion_control:Â§type: none\napprentice_code: </pre>")],
            ["Â§", "\n\t"]
        ];
    }

    function replaceAll(text, search, replace) {
        if (text.indexOf(search) >= 0) {
            text = text.replace(search, replace);
            text = replaceAll(text, search, replace);
        }
        return text;
    }

    function transform(string) {
        if (debug)
            return string;
        var res = string;
        if (res)
            for (var i = 0; i < replacements.length; i++)
                res = replaceAll(res, replacements[i][0], replacements[i][1]);
        return c ? c(res) : res;
    }

    function oldNew(stdF, oldF, newF) {
        var stdA = stdF();
        var oldA = old ? oldF() : [];
        oldA.splice(0, 1);
        var newA = !old ? newF() : [];
        newA.splice(0, 1);
        return old ? stdA.concat(oldA) : stdA.concat(newA);
    }

    function rndNum(from, to) {
        return Math.floor(rnd() * to) + from;
    }

    function up(x) {
        var res = sequence(x);
        return toUpperCase(res);
    }

    function toUpperCase(x) {
        return x.charAt(0).toUpperCase() + x.substring(1);
    }

    function toLowerCase(x) {
        return x.charAt(0).toLowerCase() + x.substring(1);
    }

    function low(x) {
        var res = sequence(x);
        return toLowerCase(res);
    }

    function sequence(x) {
        var res = "";
        for (var i = 1; i < x.length; i++)
            res += print(x[i]);
        return res;
    }

    function random(x) {
        var i = Math.floor(1 + rnd() * (x.length - 1));
        return print(x[i]);
    }

    function optional(x) {
        return rnd() >= 0.5 ? sequence(x) : "";
    }

    function print(x) {
        if (typeof x === "string") return transform(x);
        else if (typeof x === "number") return print("" + x);
        else if (typeof x === "function") return print(x());
        else if (typeof x === "object") return x[0](x);
    }

    function explain(x) {
        var res = [];
        for (var i = 1; i < x.length; i++) {
            res.push(print(x[i]));
        }
        return exp ? toLowerCase(res.join("")) : toUpperCase(res[res.length - 1]);
    }

    function s(x) {
        return sequence(x);
    }

    function r(x) {
        return random(x);
    }

    function o(x) {
        return optional(x);
    }

    function e(x) {
        return explain(x);
    }

    function start() {
        identity();
        var arr = [];
        arr.push(print("<Cards>"));
        for (var i = 1; i <= times; i++) {
            var thisSeed = seed;
            identity();
            id.counter = i;
            id.seed = old ? -thisSeed + 9 : thisSeed - 9;
            id.flaggedSeed = userFlags() + id.seed;
            arr.push(print(Card));
        }
        arr.push(print("</Cards>"));
        return arr.join("")
    }

    function Card() {
        id.name = print(id.a ? ArtifactName : Name);
        var text = print(TypeAndText);
        id.len = text.length;
        return [s,
            "<Card>", "<Identity>", id.col, id.l ? "l" : "", "</Identity>", "<Name>", id.name, "</Name>", "<Cost>", id.l ? "" : MyManaCosts, "</Cost>", Images, " ", text, "<Seed>", id.flaggedSeed, "</Seed>", "</Card>"
        ];
    }

    function Images() {
        return [s,
            "<Image>", MtgEdition, "/", Num1To149, "</Image>"
        ];
    }

    function NL() {
        return [r,
            "<NewLine />"
        ];
    }

    function TypeAndText() {
        return [s,
            id.e && id.c ?
                [s, "<Type>", "Enchantment Creature", "</Type>", "<SubType>", CreatureTypes, "</SubType>", "<Text>", EnchantmentCreatures, PowerAndToughness]
                : id.e ?
                [r, [s, "<Type>", "Enchantment &ndash; Aura", "</Type>", "<Text>", EnchantmentAuras, "</Text>"],
                    [s, "<Type>", Legend, "Enchantment", "</Type>", "<Text>", Enchantments, "</Text>"]]
                : id.a && id.c ?
                    [s, "<Type>", "Artifact Creature", "</Type>", "<SubType>", CreatureTypes, "</SubType>", "<Text>", ArtifactCreatures, PowerAndToughness]
                    : id.a ?
                        [r, [s, "<Type>", Legend, "Artifact", "</Type>", "<Text>", Artifacts, [o, NL, Artifacts], "</Text>"],
                            [s, "<Type>", Legend, "Artifact &ndash; Equipment", "</Type>", "<Text>", Equipments, "</Text>"]]
                        : id.l ?
                            [s, "<Type>", Legend, "Land", "</Type>", "<Text>", Lands, "</Text>"]
                            : id.s ?
                                [s, "<Type>", "Sorcery", [o, " &ndash; Arcane"], "</Type>", "<Text>", SpellText, "</Text>"]
                                : id.i ?
                                    [s, "<Type>", "Instant", "</Type>", "<Text>", SpellText, [o, NL, InstantActions], "</Text>"]
                                    : id.p ?
                                        [s, "<Type>", "Planeswalker", "<SubType>", CreatureTypes, "</SubType>", "<Text>", "<LoyalityActions>", LoyalityActions(1), LoyalityActions(2), LoyalityActions(3), "</LoyalityActions>", "</Text>", "<Loyalty>", PosNumber, "</Loyalty>"]
                                        : [s, "<Type>", Legend, "Creature", "</Type>", "<SubType>", CreatureTypes, "</SubType>", "<Text>", [r, Creatures, CreatureSpells, [s, Creatures, [o, NL, Creatures]]], PowerAndToughness]
        ];
    }

    function SpellText() {
        return [r,
            [s, AddCost, NL, Actions],
            [s, [o, Spells, NL], Actions, [o, NL, Actions]]
        ];
    }

    function Legend() {
        return [r,
            "", "Legendary "
        ];
    }

    function LoyalityActions(type) {
        var cost = type === 2 ? Math.floor(rnd() * -3) + 2 :
            type === 3 ? Math.floor(rnd() * -5) - 3 : Math.floor(rnd() * 1) + 1;
        return [s,
            "<LoyalityAction>", "<LoyalityCost>", cost > 0 ? "<Plus />" : cost < 0 ? "<Minus />" : "<Zero />", Math.abs(cost), "</LoyalityCost>", Actions, "</LoyalityAction>"
        ];
    }

    function ManaTypes() {
        return [r,
            "<R />", "<G />", "<B />", "<U />", "<W />"
        ];
    }

    function Colors() {
        return [r,
            "Red", "Green", "Black", "Blue", "White"
        ];
    }

    function LandTypes() {
        return [r,
            "Mountain", "Forest", "Swamp", "Island", "Plains"
        ];
    }

    function TappablePermanentTypes() {
        return [r,
            "land", "creature", "artifact"
        ];
    }

    function APermanentType() {
        return [r,
            "a land", "a creature", "an artifact", "an enchantment", "a planeswalker"
        ];
    }

    function PermanentTypes() {
        return [r,
            "land", "creature", "artifact", "enchantment", "planeswalker"
        ];
    }

    function CardTypes() {
        return [r,
            "Sorcery", "Instant", PermanentTypes
        ];
    }

    function CardTypesPlural() {
        return [r,
            "sorceries", "instants", [s, PermanentTypes, "s"]
        ];
    }

    function LowManaCosts() {
        return [s,
            [o, SmallNumberCost], ManaTypes
        ];
    }

    function MyManaCosts() {
        return [s,
            !id.cmc && !id.colors ? [s, "<Mana>", "0", "</Mana>"] :
                id.numColorless > 0 ? [s, "<Mana>", "" + id.numColorless, "</Mana>"] : "",
            id.w ? id.w : "",
            id.u ? id.u : "",
            id.r ? id.r : "",
            id.b ? id.b : "",
            id.g ? id.g : ""
        ];
    }

    function ManaCosts() {
        return [r,
            LowManaCosts,
            [s, SmallNumberCost, ManaTypes, ManaTypes],
            [s, ManaTypes, ManaTypes, ManaTypes],
            [s, PosNumberCost, ManaTypes]
        ];
    }

    function SmallNumberCost() {
        return [s,
            "<Mana>", SmallNumber, "</Mana>"
        ];
    }

    function PosNumberCost() {
        return [s,
            "<Mana>", PosNumber, "</Mana>"
        ];
    }

    function Number() {
        return [r,
            "0", PosNumber
        ];
    }

    function PosNumber() {
        return [r,
            SmallNumber, BigNumber
        ];
    }

    function SmallNumber() {
        return [r,
            "1", "2", "3", "4"
        ];
    }

    function BigNumber() {
        return [r,
            "5", "6", "7", "8", "9"
        ];
    }

    function PowerAndToughness() {
        return [s, "</Text>", "<Power>", id.pow, "</Power>", "<Toughness>", id.tou, "</Toughness>"];
    }

    function Permanents() {
        return oldNew(StdPermanents, OldPermanents, NewPermanents);
    }

    function OldPermanents() {
        return [r,
            [s, "Protection from ", [r, [low, Colors], "all colors"]],
            [s, "Cumulative upkeep ", LowManaCosts]
        ];
    }

    function StdPermanents() {
        return [r,
            [s, "Protection from ", [r, CardTypesPlural]],
            "Indestructible", "Shroud",
        ];
    }

    function NewPermanents() {
        return [r,
            "Hexproof", "Exalted"
        ];
    }

    function Actions() {
        return [s,
            ActionsWithoutDot, "."
        ];
    }

    function ActionsWithoutDot() {
        return oldNew(StdActionsWithoutDot, OldActionsWithoutDot, NewActionsWithoutDot);
    }

    function OldActionsWithoutDot() {
        return [r,
            [s, "Fateseal ", SmallNumber]
        ]
    }

    function StdActionsWithoutDot() {
        return [r,
            [s, [up, Targets], " opponent loses ", SmallNumber, " life"],
            [s, "<This /> deals ", SmallNumber, " damage to target ", [r, "creature", "player", "creature or player"]],
            [s, "You gain ", SmallNumber, " life"],
            [s, "Exile ", TargetPermanent],
            [s, "Destroy ", TargetPermanent],
            [s, "Draw ", [r, "a card", "two cards", "three cards", "four cards"]],
            [s, [up, Targets], " opponent sacrifices ", [r, APermanentType, [s, "two ", PermanentTypes, "s"]]],
            [r,
                [s, "Tap ", TappablePermanent],
                [s, "Untap ", TappablePermanent],
                [s, "Tap or untap ", TappablePermanent]
            ],
            [s, "Target creature gets ", TempBonus],
            [s, "Target creature gets ", TempMalus]
        ];
    }

    function NewActionsWithoutDot() {
        return [r,
            [s, "Scry ", SmallNumber],
            [s, "Manifest the top ", [r, "card", "two cards"], " of your library"],
            [s, "Detain ", [r,
                [s, [o, "up to one "], TappablePermanent],
                [s, "up to two ", TappablePermanent, "s"]]]
        ];
    }

    function InstantActions() {
        return [r,
            [s, "Regenerate ", [r, "target creature.", "each creature you control."]],
            [s, "Counter ", [r, "target spell.", "each spell you don't control."]]
        ];
    }

    function Targets() {
        return [r,
            "target", "each"
        ];
    }

    function TappablePermanent() {
        return [s,
            "target ", TappablePermanentTypes
        ];
    }

    function TargetPermanent() {
        return [s,
            "target ", PermanentTypes
        ];
    }

    function AddCost() {
        return [s,
            "As an additional cost to cast <This />",
            [r, ", ", [s, ", pay ", SmallNumberCost, " or "]],
            [r,
                [s, "sacrifice ", [r, "a permanent", APermanentType]],
                [s, "pay ", SmallNumber, " life"],
                [s, "discard ", [r, "a card", "two cards"]],
                [s, "tap an untapped ", TappablePermanentTypes, " you control"]
            ],
            "."
        ];
    }

    function Artifacts() {
        return [r,
            [s, [o, "Pay ", SmallNumber, " life, "], [o, SmallNumberCost, ", "], "<Tap />", ": ", Actions]
        ];
    }

    function Equipments() {
        return [s,
            [r,
                [s, "Living weapon", NL, "Equipped creature gets ", Bonus, " and has ", [low, Creatures], ".", NL],
                [s, "Equipped creature ", GetsAndHasBonus, ".", NL]],
            "Equip ", SmallNumberCost, NL
        ];
    }

    function GetsAndHasBonus() {
        return [s,
            [o, "gets ", Bonus, " and "], "has ", [low, Creatures]
        ];
    }

    function Enchantments() {
        return [s,
            [o, Permanents, NL],
            [r,
                [s, "Creatures you control have ", [low, Creatures], "."],
                [s, "Permanents you control have ", [low, Permanents], "."],
                [s, "At the beginning of your upkeep, ", [low, Actions]],
                [s, "At the beginning of each combat, ",
                    [r, "up to one target creature gets ", "creatures you control get "],
                    [low, Creatures], " until end of turn."],
                [s, "Whenever a ", [o, "non-token "], "creature ", [o, [r, "you control ", "an opponent controls "]], "dies, ", [low, Actions]],
                [s, "Whenever you draw a card, ", [low, Actions]]]
        ];
    }

    function EnchantmentAuras() {
        return [r,
            [s, "Enchant creature", NL, "Enchanted creature ", GetsAndHasBonus, [o, " and ", [low, SimpleCreatures]], "."],
            [s, "Enchant land", NL, "Enchanted land has ", [low, Permanents], "."],
            [s, "Enchant nonland permanent ", NL, "Enchanted permanent has ", [low, Permanents], "."]
        ];
    }

    function EnchantmentCreatures() {
        var ability = print(SimpleCreatures);
        return [s,
            "Bestow ", ManaCosts, NL,
            ability, NL,
            "Enchanted creature gets +", id.pow, "/+", id.tou, " and has ", ability, "."
        ];
    }

    function ArtifactCreatures() {
        return [r,
            Creatures,
            [s, "Modular ", SmallNumber],
            "Sunburst",
        ];
    }

    function TempBonus() {
        return [s,
            Bonus, " ", UntilTurn
        ];
    }

    function Bonus() {
        return [r,
            [s, "+", SmallNumber, "/+", SmallNumber],
            [s, "+1/+1 for each ", ForEachNumberOfThings]
        ];
    }

    function UntilTurn() {
        return [r,
            "until end of turn" // ,"until your next turn"
        ];
    }

    function TempMalus() {
        return [s,
            Malus, " ", UntilTurn
        ];
    }

    function Malus() {
        return [r,
            [s, "-", SmallNumber, "/-", SmallNumber],
            [s, "-1/-1 for each ", ForEachNumberOfThings]
        ];
    }

    function CreaturesWithDot() {
        return [s,
            Creatures, "."
        ];
    }

    function Creatures() {
        return [r,
            Permanents, SimpleCreatures
        ];
    }

    function SimpleCreatures() {
        return oldNew(StdSimpleCreatures, OldSimpleCreatures, NewSimpleCreatures);
    }

    function StdSimpleCreatures() {
        return [r,
            "Deathtouch", "Defender", "Double strike", "First strike", "Flying", "Haste", "Reach", "Trample", "Vigilance"
        ];
    }

    function OldSimpleCreatures() {
        return [r,
            "Banding", "Fear", "Flanking", "Horsemanship", "Shadow",
            [s, LandTypes, "walk"],
            [s, "Devour ", SmallNumber],
            [s, "Absorb ", SmallNumber],
            [s, "Amplify ", SmallNumber],
            [s, "Rampage ", PosNumber]
        ];
    }

    function NewSimpleCreatures() {
        return [r,
            "Intimidate", "Lifelink", "Menace", "Prowess", "Battle cry", "Undying", "Wither", "Changeling", "Evolve", "Infect", "Persist", "Provoke",
            [s, "Annihilator ", SmallNumber],
            [s, "Bushido ", PosNumber]
        ];
    }

    function LevelSection(from, to) {
        var toLvl = to ? ("<To>" + to + "</To>") : "<ToAny />";
        id.pow = Math.min(9, id.pow * 1 + rndNum(1, 3));
        id.tou = Math.min(9, id.tou * 1 + rndNum(1, 3));
        return [s,
            "<Level>", "<From>", from, "</From>", toLvl, Creatures, "<LevelPower>", id.pow, "</LevelPower>", "<LevelToughness>", id.tou, "</LevelToughness>", "</Level>"
        ];
    }

    function LevelUp() {
        id.level = true;
        var start1 = Math.floor(rnd() * 3) + 1;
        var start2 = start1 + Math.floor(rnd() * 3) + 2;
        return [s,
            "<Levels>",
            "<LevelUp>",
            "Level up ", LowManaCosts, "<LevelPower>", id.pow, "</LevelPower>", "<LevelToughness>", id.tou, "</LevelToughness>",
            "</LevelUp>",
            LevelSection(start1, start2 - 1),
            LevelSection(start2, undefined),
            "</Levels>"
        ];
    }

    function CreatureSpells() {
        return oldNew(StdCreatureSpells, OldCreatureSpells, NewCreatureSpells);
    }

    function StdCreatureSpells() {
        return [r,
            [s, "Morph ", ManaCosts],
            [s, "Bloodthirst ", SmallNumber]
        ];
    }

    function OldCreatureSpells() {
        return [r,
            [s, "Echo ", ManaCosts]
        ];
    }

    function NewCreatureSpells() {
        return [r,
            "Unleash",
            [s, CreatureTypes, "offering"],
            [s, ManaCosts, ": Monstrosity ", PosNumber, "."],
            [s, "Graft ", SmallNumber],
            [s, "Soulshift ", PosNumber],
            [s, "Emerge", ManaCosts],
            [s, "Scavenge ", LowManaCosts],
            [s, "Bloodthirst  ", SmallNumber],
            [s, "Bloodrush ", ManaCosts],
            [s, "Raid &ndash; ", Actions],
            [s, "Unearth ", ManaCosts],
            [s, "Dash ", ManaCosts],
            [s, "Evoke ", ManaCosts],
            [s, "Ninjutsu ", SmallNumberCost],
            [s, "Renown ", SmallNumber],
            [s, "Battalion &ndash; ", Actions],
            [s, "Haunt &ndash; When <This /> leaves the battlefield, haunted creature gets ", TempMalus],
            [s, "Grandeur &ndash; ", [e, "Discard another card named <This />: ", Actions]],
            [s, "Exploit &ndash; ", [e, "When <This /> enters the battlefield, if it exploited a creature, ", Actions]],
            [s, "Soulbond &ndash; ", [e, "As long as <This /> is soulbond, both creatures have ", CreaturesWithDot]],
            [s, "Heroic &ndash; ", [e, "Whenever you cast a spell that targets <This />,", Actions]]
        ];
    }

    function Lands() {
        return [r,
            [s, LandsMana, [o, NL, Artifacts]],
            [s, LandsMana, NL, SmallNumberCost, MyManaCosts, ": <This /> becomes a ", SmallNumber, "/", SmallNumber, " ", CreatureTypes, " creature until end of turn. It's still a land."],
            [s, "Hideaway", NL, MyManaCosts, ", <Tap />: You may play the exiled card without paying its mana cost if ", Condition, ".", NL, SimpleLandsMana],
            [s, "When <This /> enters the battlefield, ", [low, Actions], NL, SimpleLandsMana],
            [s, "<This /> enters the battlefield tapped",
                [r,
                    [s, ".", NL, LandsMana],
                    [s, [r,
                        " unless you pay 2 life",
                        " unless you control two or more basic lands",
                        [s, " unless you control a ", LandTypes],
                        [s, " unless you reveal a ", [r, LandTypes], " from your hand"],
                        " unless you control two or fewer other lands"],
                        [s, ".", NL, LandsMana]]]]
        ];
    }

    function SimpleLandsMana() {
        return [s,
            "<Tap />: Add ", ManaTypes, " to your mana pool. "
        ];
    }

    function LandsMana() {
        return [r,
            SimpleLandsMana,
            [s, [o, "<Mana>1</Mana>, "], "<Tap />: Add ", MyManaCosts, [o, ManaTypes], " to your mana pool. "]
        ];
    }

    function Condition() {
        return [r,
            [s, "you control creatures with total power of ", BigNumber, " or greater"],
            [s, "you control ", PosNumber, " or more ", PermanentTypes, "s"],
            [s, "a library has ", PosNumber, "0 or less cards"],
            [s, [r, "you have", "an opponent has"], " no cards in hand"]
        ];
    }

    function SimpleSpells() {
        return oldNew(StdSimpleSpells, OldSimpleSpells, NewSimpleSpells);
    }

    function StdSimpleSpells() {
        return [r,
            "Flash", "Storm"
        ];
    }

    function OldSimpleSpells() {
        return [r,
            "Fading", "Ripple"
        ];
    }

    function NewSimpleSpells() {
        return [r,
            "Cascade", "Cipher", "Conspire", "Convoke", "Delve", "Epic", "Extort", "Populate", "Proliferate", "Rebound", "Replicate", "Retrace", "Split second"
        ];
    }

    function Other() {
        return [r,
            "Vanishing", "Channel", "Chroma", "Imprint", "Join forces", "Kinship", "Radiance"
        ];
    }

    function Spells() {
        return oldNew(StdSpells, OldSpells, NewSpells);
    }

    function StdSpells() {
        return [r,
            SimpleSpells,
            id.numColorless > 0 ? [s, "Affinity to ", [r, LandTypes, CreatureTypes, PermanentTypes], "s"] : SimpleSpells,
            [s, "Flashback ", LowManaCosts],
            [s, "Madness ", LowManaCosts],
            [s, "Suspend ", PosNumber, " &ndash; ", ManaCosts],
            [s, [up, CardTypes], "cycling ", LowManaCosts],
            [s, "Hellbent &ndash; ", [e, "If you have no cards in hand, ", Actions]],
            [s, "Threshold &ndash; ", [e, "If you have seven or more cards in your graveyard, ", [low, Actions]]]
        ];
    }

    function OldSpells() {
        return [r,
            [s, "Buyback ", LowManaCosts],
            [s, "Kicker ", LowManaCosts, NL, [e, "If you paid the kicker cost, ", [low, Actions]]],
            [s, "Multikicker ", LowManaCosts, NL, "For each time this spell was kicked, ", [low, Actions]]
        ];
    }

    function NewSpells() {
        return [r,
            [s, "Clash &ndash; ", Actions],
            [s, "Cycling ", ManaTypes],
            [s, "Dredge ", SmallNumber],
            //[s,"Entwine ",SmallNumberCost],
            [s, "Miracle ", LowManaCosts],
            //[s,"Overload ",ManaCosts],
            [s, "Recover ", LowManaCosts],
            [s, "Reinforce ", SmallNumber, " &ndash; ", LowManaCosts],
            [s, "Splice onto Arcane"],
            [s, "Transmute ", LowManaCosts],
            [s, "Bolster ", SmallNumber],
            [s, "Prowl ", LowManaCosts, " &ndash; ", Actions],
            [s, "Delirium &ndash; ", Actions],
            [s, "Formidable &ndash; ", Actions],
            [s, "Domain &ndash; For each land type among lands you control, ", [low, Actions]],
            [s, "Fateful hour &ndash; ", [e, "If you have 5 or less life, ", Actions]],
            [s, "Ferocious &ndash; ", [e, "If you control a creature with power 4 or greater, "], Actions],
            [s, "Landfall &ndash; ", [e, "If you had a land enter the battlefield under your control this turn, ", Actions]],
            [s, "Metalcraft &ndash; ", [e, "If you control three or more artifacts, ", Actions]],
            [s, "Morbid &ndash; ", [e, "If a creature died this turn, ", Actions]],
            [s, "Spell mastery &ndash; ", [e, "If there are two or more instant and/or sorcery cards in your graveyard, ", Actions]]
        ];
    }

    function Name() {
        return [r,
            [s, Adjective, " ", Noun],
            [s, Noun, " ", Suffix]
        ];
    }

    function Suffix() {
        return [s,
            "of ", SuffixNouns
        ];
    }

    function SuffixNouns() {
        return [r,
            "Wards", "Maze", "Vortex", "Pits", "Suffering", "Prince", "Apocalypse", "Secrets", "Angels", "Confusion", "Death", "Torture", "Suffering", "Visions", "Disorder", "Winds", "Particles", "Research", "Future", "Rivers", "Death", "East", "South", "Death", "Glaciers", "Abjuration", "Blade", "Mystery", "Ring", "Silence", "Lute", "Skull", "Lute", "Ring", "Storm", "Fear", "Eternity", "Unnamed", "Pain", "Joy", "Gates", "Heroes", "Justice", "Future"
        ];
    }

    function Adjective() {
        return [r,
            "Blind", "Cursed", "Dreadful", "Eternal", "Ethereal", "Grand", "Foul", "Gray", "High", "Hunting", "Silent", "Inverted", "Low", "Wild", "Working", "Essential", "Ghostly"
        ];
    }

    function Noun() {
        return [r,
            "Conjuration", "Ceremony", "Comparison", "Cabbalism", "Alchemy", "Experiment", "Paradox", "Sacrament", "Rite", "Invocation", "Sacrament", "Evocation", "Sacrifice", "Philosophy", "Academics", "Divination", "History", "Science", "Cube", "Portal", "Spear", "Shield", "Wand", "Ground", "Outlands", "Paradise", "Kingdom", "Vorticies", "Hall", "Limbo", "Domains", "Abyss", "Valley", "Grounds", "Prisons"
        ];
    }

    function ArtifactName() {
        return [s,
            [r, "Tin", "Platinum", "Adamant", "Bronze", "Steel", "Soulsilver", "Windmetal", "Orichalcum", "Soulcopper", "Lead", "Mithril", "Silver", "Earthplatinum"], " ", SuffixNouns
        ];
    }

    function CreatureTypes() {
        return [r,
            VowelCreatureTypes, ConsonantCreatureTypes
        ];
    }

    function CreatureTypePlural() {
        return [r,
            [r,
                [s, [r, RegVowelCreatureTypes, RegConsonantCreatureTypes], "s"],
                IrregVowelCreatureTypesPlural,
                IrregConsonantCreatureTypesPlural
            ]
        ];
    }

    function VowelCreatureTypes() {
        return [r, RegVowelCreatureTypes, RegVowelCreatureTypes];
    }

    function RegVowelCreatureTypes() {
        return [r,
            "Advisor", "Angel", "Ape", "Archer", "Archon", "Artificer", "Assassin", "Assembly-Worker", "Atog", "Aurochs", "Avatar", "Efreet", "Eldrazi", "Elemental", "Elephant", "Elk", "Illusion", "Imp", "Incarnation", "Ogre", "Ooze", "Orc", "Unicorn"
        ];
    }

    function IrregVowelCreatureTypes() {
        return [r,
            "Ally", "Elf", "Octopus", "Ox"
        ];
    }

    function IrregVowelCreatureTypesPlural() {
        return [r,
            "Allies", "Elves", "Octopusses", "Oxes"
        ];
    }

    function ConsonantCreatureTypes() {
        return [r, RegConsonantCreatureTypes, IrregConsonantCreatureTypes];
    }

    function RegConsonantCreatureTypes() {
        return [r,
            "Badger", "Barbarian", "Basilisk", "Bat", "Bear", "Beast", "Berserker", "Bird", "Blinkmoth", "Boar", "Bringer", "Carrier", "Cat", "Centaur", "Cephalid", "Chimera", "Citizen", "Cleric", "Cockatrice", "Construct", "Crab", "Crocodile", "Dauthi", "Demon", "Deserter", "Devil", "Djinn", "Dragon", "Drake", "Dreadnought", "Drone", "Druid", "Dryad", "Faerie", "Frog", "Gargoyle", "Germ", "Giant", "Gnome", "Goat", "Goblin", "God", "Golem", "Gorgon", "Gremlin", "Griffin", "Hag", "Hellion", "Hippo", "Hippogriff", "Homarid", "Horror", "Horse", "Hound", "Human", "Hydra", "Hyena", "Insect", "Juggernaut", "Kavu", "Kirin", "Kithkin", "Knight", "Kobold", "Kraken", "Lamia", "Lammasu", "Leviathan", "Lhurgoyf", "Lizard", "Manticore", "Masticore", "Minion", "Minotaur", "Monger", "Mongoose", "Monk", "Mutant", "Myr", "Mystic", "Naga", "Nephilim", "Nightmare", "Nightstalker", "Ninja", "Noggle", "Nomad", "Pentavite", "Pest", "Phelddagrif", "Phoenix", "Pincher", "Pirate", "Plant", "Praetor", "Prism", "Processor", "Rat", "Rebel", "Reflection", "Rhino", "Rigger", "Rogue", "Sable", "Salamander", "Samurai", "Sand", "Saproling", "Satyr", "Scarecrow", "Scion", "Scorpion", "Scout", "Serf", "Serpent", "Shade", "Shaman", "Shapeshifter", "Sheep", "Siren", "Skeleton", "Slith", "Sliver", "Slug", "Snake", "Soldier", "Soltari", "Spawn", "Specter", "Spellshaper", "Spider", "Spike", "Spirit", "Splinter", "Sponge", "Squid", "Squirrel", "Surrakar", "Survivor", "Tetravite", "Thopter", "Thrull", "Triskelavite", "Troll", "Turtle", "Vampire", "Viashino", "Volver", "Wall", "Warrior", "Weird", "Whale", "Wizard", "Wolverine", "Worm", "Wraith", "Zombie"
        ];
    }

    function IrregConsonantCreatureTypes() {
        return [r,
            "Cyclops", "Dwarf", "Fish", "Fox", "Fungus", "Harpy", "Homunculus", "Kor", "Leech", "Mercenary", "Merfolk", "Moonfolk", "Nymph", "Pegasus", "Sphinx", "Treefolk", "Vedalken", "Werewolf", "Wolf"
        ];
    }

    function IrregConsonantCreatureTypesPlural() {
        return [r,
            "Cyclopses", "Dwarves", "Fish", "Foxes", "Fungus", "Harpies", "Homunculi", "Kor", "Leeches", "Mercenaries", "Merfolk", "Moonfolk", "Nymph", "Pegasi", "Sphinxes", "Starfish", "Treefolk", "Vedalken", "Werewolves", "Wolves"
        ];
    }

    function NumWord() {
        return [r,
            "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen"
        ];
    }

    function Num1To149() {
        return [r,
            [s, "120"],
            [s, "1", [r, "0", "1"], Number],
            [s, PosNumber, Number],
            [s, PosNumber]
        ];
    }

    function MtgEdition() {
        return [r,
            "bfz", "ogw", "dtk", "frf", "ktk", "ori", "jou",
            "kld", "aer", "ixa", "rix",
            "gtc", "dgm", "rtr", "avr", "dka", "isd", "roe", "wwk", "zen",
            "ala", "eve", "shm", "lw",
            "m19", "m15", "m14", "m13", "m12", "m11", "m10", "10e", "9ed", "8ee"
        ];
    }

    return start();
}

generateStringFromSeed(/*value-here*/)