package utils

import play.api.http.HttpFilters
import play.filters.csrf.CSRFFilter
import javax.inject.Inject

import filters.ExampleFilter

class Filters @Inject() (csrfFilter: CSRFFilter, exampleFilter: ExampleFilter) extends HttpFilters {
  def filters = Seq(csrfFilter)
}