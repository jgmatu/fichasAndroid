package protocol

import (
      "io"
      "fmt"
      "strings"
)

type Entry struct {
      image string
      loc *Location
}

func NewEntryFillOut(i string, l *Location) *Entry {
      return &Entry{i, l}
}

func NewEntry(dir string) *Entry  {
      return &Entry{dir, NewLocation()}
}

func (e *Entry) Write(tx io.Writer) {
      WriteString(tx, e.getNameImg())
      WriteImage(tx, e.image)
      e.loc.Write(tx)
}

func (e *Entry) Read(rx io.Reader) {
      e.image += "/" + ReadString(rx)

      ReadImage(rx, e.image)
      e.loc.Read(rx)
}

func (e Entry) String() string {
      return fmt.Sprintf("Entry\nImage : %s\nLocation : %s\n", e.image, e.loc)
}

func (e Entry) Equals(en Entry) bool {
      return e.image == en.image && e.loc.Equals(en.loc)
}

func (e Entry) FileFormat() string {
      return fmt.Sprintf("%s,%s",e.loc.FileFormat() , e.image)
}

func (e Entry) IsMaxDistance(loc Location) bool {
      return e.loc.IsMaxDistance(loc)
}

func (e Entry) getNameImg() string {
      routes := strings.Split(e.image, "/")

      return routes[len(routes) - 1]
}
