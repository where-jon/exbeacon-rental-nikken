package services.manage

import javax.inject.Inject

class CompanyService @Inject()(companyDAO: models.manage.companyDAO) {
  def selectCompanyByPlaceId(placeId: Int, companyName: String = "") = companyDAO.selectCompany(placeId, companyName)
  def selectCarReserveCheck( placeId: Int, companyId: Int)= companyDAO.selectCarReserveCheck(placeId, companyId)
  def insertCompany(companyName: String, note: String, placeId: Int) = companyDAO.insert(companyName, note: String, placeId)
  def updateCompanyById(companyId: Int, companyName: String, note: String) = companyDAO.updateById(companyId, companyName, note)
  def deleteCompanyById(companyId: Int) = companyDAO.deleteById(companyId)
}
