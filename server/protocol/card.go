package protocol

import (
      "io"
      "fmt"
      "os"
)

const (
      LENCARDTXT = 6.
      LENROUTETXT = 5
)

type MetaCard struct {
      id int32
      name string
      description string
      skill string
      category string
}

type Card struct {
      head *MetaCard
      entries []Entry
}

func NewMeta(i int32, n, d, s, c string) *MetaCard {
      return &MetaCard{i, n, d, s, c}
}

func NewCardFillOut(h *MetaCard, e []Entry) *Card {
      return &Card{h, e}
}

func NewCard() *Card {
      return &Card{}
}

func (h *MetaCard) IsEquals(i int32, n string) bool {
      return i == h.id && n == h.name
}


func (c *Card) Write(tx io.Writer) {
      writeHead(tx, c.head)

      WriteInt32(tx, int32(len(c.entries)))
      for _,e := range c.entries {
            e.Write(tx)
      }
}

func (c *Card) Read(rx io.Reader) {
      c.head = readHead(rx)

      dir := fmt.Sprintf("images/%d%s",c.head.id, c.head.name)
      c.CreateDir(dir)

      length := ReadInt32(rx)
      c.entries = make([]Entry, length)
      for i, e := range c.entries {
            e = *NewEntry(dir)
            e.Read(rx)
            c.entries[i] = e
      }
}

func (c Card) String() string {
      result := fmt.Sprintf("Name : %s\nDescription : %s\nSkill : %s\n" +
            "Category : %s\n", c.head.name, c.head.description, c.head.skill, c.head.category)

      for _, e := range c.entries {
            result += fmt.Sprintf("Entry : %s\n", e)
      }
      return result
}

func (c Card) FileFormatCard() string {
      return fmt.Sprintf("%d,%s,%s,%s,%s\n", c.head.id, c.head.name, c.head.description,
            c.head.skill, c.head.category)
}

func (c Card) FileFormatEntries() string {
      result := ""

      for _, e := range c.entries {
            result += fmt.Sprintf("%d,%s,%s\n",c.head.id , c.head.name, e.FileFormat())
      }
      return result
}

func (c Card) Equals(crd Card) bool {
      return c.head.name == crd.head.name && c.head.description == crd.head.description &&
            c.head.skill == crd.head.skill && c.head.category == crd.head.category &&
            isEntries(c.entries, crd.entries)
}


func (c Card) IsMaxDistance(loc Location) bool {
      for _, e := range c.entries {
            if !e.IsMaxDistance(loc) {
                  return false
            }
      }
      return true
}

func (c Card) CreateDir(dir string) error {
      return os.MkdirAll(dir, 0700)
}

func isEntries(src []Entry, dst []Entry) bool {
      if len(src) != len(dst) {
            return false
      }
      for i := 0 ; i < len(src) ; i++ {
            if !src[i].Equals(dst[i]) {
                  return false
            }
      }
      return true
}

func writeHead(tx io.Writer, h *MetaCard) {

      WriteInt32(tx, h.id)
      WriteString(tx, h.name)
      WriteString(tx, h.description)
      WriteString(tx, h.skill)
      WriteString(tx, h.category)
}

func readHead(rx io.Reader) *MetaCard {
      head := &MetaCard{}

      head.id = ReadInt32(rx)
      head.name = ReadString(rx)
      head.description = ReadString(rx)
      head.skill = ReadString(rx)
      head.category = ReadString(rx)
      return head
}
